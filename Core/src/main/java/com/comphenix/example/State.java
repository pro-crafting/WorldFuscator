package com.comphenix.example;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;

public class State {

  private Material material;
  private int data;

  public State(Material type, int data) {
    this.material = type;
    this.data = data;
  }

  public State(WrappedBlockData blockData) {
    this(blockData.getType(), blockData.getData());
  }

  public Material getMaterial() {
    return material;
  }

  public int getData() {
    return data;
  }

  @Override
  public String toString() {
    return material + ":" + data;
  }
}