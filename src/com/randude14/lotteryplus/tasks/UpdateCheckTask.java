package com.randude14.lotteryplus.tasks;

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;

public class UpdateCheckTask implements Task {
	private static final String currentVersion = Plugin.getVersion();
	private int updateId;
	
	public void run() {
		String newestVersion = updateCheck(currentVersion);
		if(!newestVersion.endsWith(currentVersion)) {
			Logger.info("A new version is available, Current Version: %s, New Version: %s.", currentVersion, newestVersion);
		}
	}
	
	private String updateCheck(String currentVersion) {
		try {
			URL url = new URL(
					"http://dev.bukkit.org/server-mods/lotteryplus/files.rss");
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nodes = doc.getElementsByTagName("item");
			Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				Element firstElement = (Element) firstNode;
				NodeList firstElementTagName = firstElement
						.getElementsByTagName("title");
				Element firstNameElement = (Element) firstElementTagName
						.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();
				return firstNodes.item(0).getNodeValue();
			}
		} catch (Exception ex) {
		}

		return currentVersion;
	}
	
	public void scheduleTask() {
		Plugin.cancelTask(updateId);
		long delay = Config.getProperty(Config.UPDATE_DELAY) * SERVER_SECOND * MINUTE;
		updateId = Plugin.scheduleSyncRepeatingTask(this, 0L, delay);
	}
}
