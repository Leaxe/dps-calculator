package com.duckblade.osrs.dpscalc.ui.skills;

import com.google.common.collect.ImmutableMap;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;

import static net.runelite.api.Skill.*;

@Singleton
public class SkillsPanel extends JPanel
{

	private final Client client;
	private final ClientThread clientThread;

	private final Map<Skill, StatBox> statBoxes;
	private final Map<Skill, StatBox> boostBoxes;

	@Inject
	public SkillsPanel(Client client, ClientThread clientThread)
	{
		this.client = client;
		this.clientThread = clientThread;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JButton loadFromClientButton = new JButton("Load From Client");
		loadFromClientButton.addActionListener(e -> loadFromClient());
		loadFromClientButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(loadFromClientButton);

		add(Box.createVerticalStrut(10));

		statBoxes = new ImmutableMap.Builder<Skill, StatBox>()
				.put(ATTACK, new StatBox("att", true))
				.put(STRENGTH, new StatBox("str", true))
				.put(DEFENCE, new StatBox("def", true))
				.put(MAGIC, new StatBox("mage", true))
				.put(RANGED, new StatBox("range", true))
				.put(PRAYER, new StatBox("prayer", true))
				.build();
		add(new StatCategory("Player Stats", new ArrayList<>(statBoxes.values())));

		add(Box.createVerticalStrut(10));

		boostBoxes = new ImmutableMap.Builder<Skill, StatBox>()
				.put(ATTACK, new StatBox("att", true))
				.put(STRENGTH, new StatBox("str", true))
				.put(DEFENCE, new StatBox("def", true))
				.put(MAGIC, new StatBox("mage", true))
				.put(RANGED, new StatBox("range", true))
				.put(PRAYER, new StatBox("prayer", true))
				.build();
		add(new StatCategory("Boosts", new ArrayList<>(boostBoxes.values())));
	}

	private void loadFromClient()
	{
		if (client == null || clientThread == null)
			return; // ui test

		clientThread.invokeLater(() ->
		{
			Player p = client.getLocalPlayer();
			if (p == null)
				return;

			statBoxes.forEach((skill, box) -> box.setValue(client.getRealSkillLevel(skill)));
		});
	}

	public boolean isReady()
	{
		return statBoxes.values()
				.stream()
				.noneMatch(sb -> sb.getValue() == 0);
	}

	public String getSummary()
	{
		if (isReady())
			return "Set";

		return "Not Set";
	}

	public Map<Skill, Integer> getSkills()
	{
		Map<Skill, Integer> results = new HashMap<>(6);
		statBoxes.forEach((k, v) -> results.put(k, v.getValue()));
		return results;
	}

	public void setSkills(Map<Skill, Integer> newSkills)
	{
		newSkills.forEach((s, v) -> statBoxes.get(s).setValue(v));
	}

	public Map<Skill, Integer> getBoosts()
	{

		Map<Skill, Integer> results = new HashMap<>(6);
		boostBoxes.forEach((k, v) -> results.put(k, v.getValue()));
		return results;
	}

	public void setBoosts(Map<Skill, Integer> newSkills)
	{
		newSkills.forEach((s, v) -> boostBoxes.get(s).setValue(v));
	}
}
