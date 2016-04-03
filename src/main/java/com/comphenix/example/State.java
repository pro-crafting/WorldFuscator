package com.comphenix.example;

public class State {
	private int id;
	private int data;

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
		return id+":"+data;
	}
}