package com.comphenix.example;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;

public class State {

  private int id;
  private int data;

  public State(Material type, int data) {
    this(type.getId(), data);
  }

  public State(WrappedBlockData blockData) {
    this(blockData.getType(), blockData.getData());
  }

  public State(int id, int data) {
    this.id = id;
    this.data = data;
  }

  public int getId() {
    return id;
  }

  public int getData() {
    return data;
  }

  @Override
  public String toString() {
    return id + ":" + data;
  }
}