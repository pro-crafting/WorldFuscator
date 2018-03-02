package net.myplayplanet.worldfuscator.Core;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {

  private List<Integer> hideIds;
  private boolean debugEnabled;

  public Configuration(FileConfiguration configuration) {
    this.hideIds = configuration.getIntegerList("hidden");
    this.debugEnabled = configuration.getBoolean("debug.enabled", false);
  }

  public int getObfuscationBlock() {
    return 121;
  }

  public List<Integer> getHideIds() {
    return this.hideIds;
  }

  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  public void setDebugEnabled(boolean debugEnabled) {
    this.debugEnabled = debugEnabled;
  }
}
