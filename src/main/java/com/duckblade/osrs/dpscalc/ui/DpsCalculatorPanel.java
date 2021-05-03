package com.duckblade.osrs.dpscalc.ui;

import com.duckblade.osrs.dpscalc.calc.CalcInput;
import com.duckblade.osrs.dpscalc.calc.CalcManager;
import com.duckblade.osrs.dpscalc.model.EquipmentStats;
import com.duckblade.osrs.dpscalc.model.ItemStats;
import com.duckblade.osrs.dpscalc.model.NpcStats;
import com.duckblade.osrs.dpscalc.model.WeaponMode;
import com.duckblade.osrs.dpscalc.ui.equip.EquipmentPanel;
import com.duckblade.osrs.dpscalc.ui.npc.NpcStatsPanel;
import com.duckblade.osrs.dpscalc.ui.skills.SkillsPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

@Singleton
public class DpsCalculatorPanel extends PluginPanel
{
	private final CalcManager calcManager;

	private final JPanel contentPanel;
	private final JPanel menuPanel;

	private final MenuPanelNavEntry npcStatsNav;
	private final NpcStatsPanel npcStatsPanel;

	private final MenuPanelNavEntry equipmentNav;
	private final EquipmentPanel equipmentPanel;

	private final MenuPanelNavEntry skillsNav;
	private final SkillsPanel skillsPanel;

	private final JLabel dpsValue;
	private static final String DPS_CALC_FAIL = "???";
	private static final DecimalFormat DPS_FORMAT = new DecimalFormat("#.###");

	private static final String GITHUB_LINK = "https://github.com/LlemonDuck/dps-calculator";

	@Inject
	public DpsCalculatorPanel(CalcManager calcManager, NpcStatsPanel npcStatsPanel, EquipmentPanel equipmentPanel, SkillsPanel skillsPanel)
	{
		super(false);
		this.calcManager = calcManager;

		this.npcStatsPanel = npcStatsPanel;
		this.equipmentPanel = equipmentPanel;
		this.skillsPanel = skillsPanel;

		setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 0));
		setLayout(new BorderLayout());

		contentPanel = new JPanel();
		contentPanel.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 0));
		JScrollPane contentScrollPane = new JScrollPane(contentPanel);
		contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(contentScrollPane, BorderLayout.CENTER);

		menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		contentPanel.add(menuPanel);

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		add(headerPanel, BorderLayout.NORTH);

		JButton homeButton = new JButton("Home");
		homeButton.setPreferredSize(new Dimension(60, 30));
		homeButton.setForeground(Color.white);
		homeButton.addActionListener(e -> openMenu());
		headerPanel.add(homeButton, BorderLayout.WEST);

		JLabel titleLabel = new JLabel("DPS Calc", JLabel.CENTER);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		headerPanel.add(titleLabel, BorderLayout.CENTER);

		ImageIcon ghIcon = new ImageIcon(ImageUtil.resizeImage(ImageUtil.loadImageResource(getClass(), "gh_logo.png"), 25, 25));
		JButton linkToGh = new JButton(ghIcon);
		linkToGh.setPreferredSize(new Dimension(60, 30));
		linkToGh.addActionListener(e -> openGhLink());
		headerPanel.add(linkToGh, BorderLayout.EAST);

		npcStatsNav = new MenuPanelNavEntry("NPC Stats", "Not Set", () -> openNpcStats(null));
		menuPanel.add(npcStatsNav);
		menuPanel.add(Box.createVerticalStrut(5));

		equipmentNav = new MenuPanelNavEntry("Equipment", "Not Set", this::openEquipment);
		menuPanel.add(equipmentNav);
		menuPanel.add(Box.createVerticalStrut(5));

		skillsNav = new MenuPanelNavEntry("Skills", "Not Set", this::openSkills);
		menuPanel.add(skillsNav);

		menuPanel.add(Box.createVerticalStrut(20));

		Font originalBold = FontManager.getRunescapeBoldFont();
		Font dpsFont = originalBold.deriveFont(originalBold.getSize() * 2f);
		JLabel dpsHeader = new JLabel("DPS:", JLabel.CENTER);
		dpsHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
		dpsHeader.setForeground(Color.white);
		dpsHeader.setFont(dpsFont);
		menuPanel.add(dpsHeader);

		dpsValue = new JLabel(DPS_CALC_FAIL, JLabel.CENTER);
		dpsValue.setAlignmentX(Component.CENTER_ALIGNMENT);
		dpsValue.setForeground(Color.white);
		dpsValue.setFont(dpsFont);
		menuPanel.add(dpsValue);
	}

	private void openGhLink()
	{
		if (!Desktop.isDesktopSupported())
		{
			JOptionPane.showMessageDialog(this, "Your desktop environment does not support opening links automatically. " +
					"You can file issues, read the source, or contribute to this plugin at " + GITHUB_LINK);
			return;
		}

		try
		{
			URI ghUri = URI.create(GITHUB_LINK);
			Desktop.getDesktop().browse(ghUri);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Your desktop environment does not support opening links automatically. " +
					"You can file issues, read the source, or contribute to this plugin at " + GITHUB_LINK);
		}
	}

	private void calculateDps()
	{
		if (!npcStatsPanel.isReady() || !equipmentPanel.isReady() || !skillsPanel.isReady())
		{
			dpsValue.setText(DPS_CALC_FAIL);
			return;
		}

		WeaponMode weaponMode = equipmentPanel.getWeaponMode();
		Map<EquipmentInventorySlot, ItemStats> equipment = equipmentPanel.getEquipment();

		CalcInput input = CalcInput.builder()
				.npcTarget(npcStatsPanel.toNpcStats())
				.combatMode(weaponMode.getMode())
				.weaponMode(weaponMode)
				.playerEquipment(equipment)
				.equipmentStats(EquipmentStats.fromMap(equipment, weaponMode, equipmentPanel.getTbpDarts()))
				.playerSkills(skillsPanel.getSkills())
				.playerBoosts(skillsPanel.getBoosts())
				.spell(equipmentPanel.getSpell())
				.build();

		float dps = calcManager.calculateDPS(input);
		dpsValue.setText(DPS_FORMAT.format(dps));
	}

	public void openMenu()
	{
		SwingUtilities.invokeLater(() ->
		{
			contentPanel.removeAll();
			contentPanel.add(menuPanel);
			npcStatsNav.setDescription(npcStatsPanel.getSummary());
			equipmentNav.setDescription(equipmentPanel.getSummary());
			skillsNav.setDescription(skillsPanel.getSummary());
			calculateDps();
			revalidate();
			repaint();
		});
	}

	public void openNpcStats(NpcStats preload)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (preload != null)
				npcStatsPanel.loadNpcStats(preload);
			contentPanel.removeAll();
			contentPanel.add(npcStatsPanel);
			revalidate();
			repaint();
		});
	}

	public void openEquipment()
	{
		SwingUtilities.invokeLater(() ->
		{
			contentPanel.removeAll();
			contentPanel.add(equipmentPanel);
			revalidate();
			repaint();
		});
	}

	public void openSkills()
	{
		SwingUtilities.invokeLater(() ->
		{
			contentPanel.removeAll();
			contentPanel.add(skillsPanel);
			revalidate();
			repaint();
		});
	}

}
