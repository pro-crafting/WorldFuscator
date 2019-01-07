package com.comphenix.example;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;

// TODO: Remove this class
public class State {

  private Material material;

  public State(Material type) {
    this.material = type;
  }

  public State(WrappedBlockData blockData) {
    this(blockData.getType());
  }

  public Material getMaterial() {
    return material;
  }

  @Override
  public String toString() {
    return material + "";
  }
}