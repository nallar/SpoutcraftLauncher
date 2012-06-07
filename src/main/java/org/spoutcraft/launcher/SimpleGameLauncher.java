/*
 * This file is part of Spoutcraft Launcher.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * Spoutcraft Launcher is licensed under the SpoutDev License Version 1.
 *
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.launcher;

import javax.swing.JOptionPane;
import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.spoutcraft.launcher.api.Event;
import org.spoutcraft.launcher.api.GameLauncher;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.api.skin.exceptions.CorruptedMinecraftJarException;
import org.spoutcraft.launcher.api.skin.exceptions.MinecraftVerifyException;
import org.spoutcraft.launcher.api.util.Resources;
import org.spoutcraft.launcher.api.util.Utils;
import org.spoutcraft.launcher.launch.MinecraftAppletEnglober;
import org.spoutcraft.launcher.launch.MinecraftLauncher;

public class SimpleGameLauncher extends GameLauncher implements WindowListener {
	private static final long serialVersionUID = 454654568463524665L;
	private MinecraftAppletEnglober minecraft;
	public static final int RETRYING_LAUNCH = -1;
	public static final int ERROR_IN_LAUNCH = 0;
	public static final int SUCCESSFUL_LAUNCH = 1;

	public SimpleGameLauncher() {
		super("Spoutcraft");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((dim.width - 870) / 2, (dim.height - 518) / 2);
		this.setSize(new Dimension(870, 518));
		this.setResizable(true);
		this.addWindowListener(this);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Resources.spoutcraftIcon));
	}

	@Override
	public void runGame(String user, String session, String downloadTicket, String mcpass) {
		((SimpleGameUpdater) Launcher.getGameUpdater()).setWaiting(true);
		while (!((SimpleGameUpdater) Launcher.getGameUpdater()).isFinished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
			}
		}
		Applet applet = null;
		try {
			applet = MinecraftLauncher.getMinecraftApplet();
		} catch (CorruptedMinecraftJarException corruption) {
			corruption.printStackTrace();
		} catch (MinecraftVerifyException verify) {
			Launcher.clearCache();
			JOptionPane.showMessageDialog(getParent(), "Your Minecraft installation is corrupt, but has been cleaned. \nTry to login again. If that fails, close and \nrestart the appplication.");
			this.setVisible(false);
			this.dispose();
			return;
		}
		if (applet == null) {
			String message = "Failed to launch Spoutcraft!";
			this.setVisible(false);
			JOptionPane.showMessageDialog(getParent(), message);
			this.dispose();
			return;
		}

		minecraft = new MinecraftAppletEnglober(applet);

		minecraft.addParameter("username", user);
		minecraft.addParameter("sessionid", session);
		minecraft.addParameter("downloadticket", downloadTicket);
		minecraft.addParameter("mppass", mcpass);
		minecraft.addParameter("spoutcraftlauncher", "true");
		minecraft.addParameter("portable", Utils.getStartupParameters().isPortable() + "");
		if (Utils.getStartupParameters().getServer() != null) {
			minecraft.addParameter("server", Utils.getStartupParameters().getServer());
			if (Utils.getStartupParameters().getPort() != null) {
				minecraft.addParameter("port", Utils.getStartupParameters().getPort());
			}
		}

		applet.setStub(minecraft);

		this.add(minecraft);

		validate();
		this.setVisible(true);

		minecraft.init();
		minecraft.setSize(getWidth(), getHeight());

		minecraft.start();

		Launcher.getSkinManager().getEnabledSkin().getLoginFrame().onRawEvent(Event.GAME_LAUNCH);
		return;
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		if (this.minecraft != null) {
			this.minecraft.stop();
			this.minecraft.destroy();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
		}
		System.out.println("Exiting Spoutcraft Launcher");
		for (Frame f : Frame.getFrames()) {
			f.dispose();
		}
		System.exit(0);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
