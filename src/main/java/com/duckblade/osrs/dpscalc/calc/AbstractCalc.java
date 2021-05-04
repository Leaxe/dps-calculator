package com.duckblade.osrs.dpscalc.calc;

public abstract class AbstractCalc
{
	private static final float SECONDS_PER_TICK = 0.6f;
	
	abstract int attackRoll(CalcInput input);

	abstract int defenseRoll(CalcInput input);

	abstract int maxHit(CalcInput input);
	
	float hitChance(int attRoll, int defRoll)
	{
		
		if (attRoll > defRoll)
			return  1f - ((defRoll + 2f) / (2f * attRoll + 1f));
		else
			return attRoll / (2f * defRoll + 1f);
	}
	
	CalcResult calculateDPS(CalcInput input)
	{
		int attRoll = attackRoll(input);
		int defRoll = defenseRoll(input);
		int maxHit = maxHit(input);
		float hitChance = hitChance(attRoll, defRoll);
		
		float weaponSpeed = input.getEquipmentStats().getSpeed();
		float dps = (maxHit * hitChance) / (2f * weaponSpeed * SECONDS_PER_TICK);
		return CalcResult.builder()
				.attackRoll(attRoll)
				.defenseRoll(defRoll)
				.maxHit(maxHit)
				.hitChance(hitChance)
				.hitRate(weaponSpeed * SECONDS_PER_TICK)
				.dps(dps)
				.build();
	}
	
}
