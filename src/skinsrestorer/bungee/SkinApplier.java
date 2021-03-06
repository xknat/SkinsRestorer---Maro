package skinsrestorer.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;
import skinsrestorer.shared.storage.SkinStorage;
import skinsrestorer.shared.utils.ReflectionUtil;

public class SkinApplier {

	public static void applySkin(final ProxiedPlayer p) {
		ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					Property textures = (Property) SkinStorage.getOrCreateSkinForPlayer(p.getName());

					InitialHandler handler = (InitialHandler) p.getPendingConnection();

					if (handler.isOnlineMode()) {
						sendUpdateRequest(p, textures);
						return;
					}

					LoginResult profile = new LoginResult(p.getUniqueId().toString(), new Property[] { textures });
					Property[] present = profile.getProperties();
					Property[] newprops = new Property[present.length + 1];
					System.arraycopy(present, 0, newprops, 0, present.length);
					newprops[present.length] = textures;
					profile.getProperties()[0].setName(newprops[0].getName());
					profile.getProperties()[0].setValue(newprops[0].getValue());
					profile.getProperties()[0].setSignature(newprops[0].getSignature());
					ReflectionUtil.setObject(InitialHandler.class, handler, "loginProfile", profile);

					if (SkinsRestorer.getInstance().isMultiBungee())
						sendUpdateRequest(p, textures);
					else
						sendUpdateRequest(p, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	public static void removeSkin(final ProxiedPlayer p) {
		ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					Property textures = (Property) SkinStorage.createProperty("textures", "", "");

					InitialHandler handler = (InitialHandler) p.getPendingConnection();

				    //Doesn't really care if you are premium now :D
					//This means fuk your premium skin. I'm putting mine on xD
					LoginResult profile = new LoginResult(p.getUniqueId().toString(), new Property[] { textures });
					Property[] present = profile.getProperties();
					Property[] newprops = new Property[present.length + 1];
					System.arraycopy(present, 0, newprops, 0, present.length);
					newprops[present.length] = textures;
					profile.getProperties()[0].setName(newprops[0].getName());
					profile.getProperties()[0].setValue(newprops[0].getValue());
					profile.getProperties()[0].setSignature(newprops[0].getSignature());
					ReflectionUtil.setObject(InitialHandler.class, handler, "loginProfile", profile);

					if (SkinsRestorer.getInstance().isMultiBungee())
						sendUpdateRequest(p, textures);
					else
						sendUpdateRequest(p, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	public static void removeOnQuit(final ProxiedPlayer p) {
		sendRemoveRequest(p);
	}

	private static void sendUpdateRequest(ProxiedPlayer p, Property textures) {
		if (p.getServer() == null)
			return;

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("SkinUpdate");

			if (textures != null) {
				out.writeUTF(textures.getName());
				out.writeUTF(textures.getValue());
				out.writeUTF(textures.getSignature());
			}

			p.getServer().sendData("SkinsRestorer", b.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void sendRemoveRequest(ProxiedPlayer p) {
		if (p.getServer() == null)
			return;

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("SkinRemove");

			p.getServer().sendData("SkinsRestorer", b.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}