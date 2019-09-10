package com.npdevs.blowthegarbage;

import android.media.Image;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;

public class SampleItem {

	private String name;
	private boolean organic,severe;
	private int upvote;
	private String garbage;
	private Button approved,disapproved;

	public SampleItem(String name, boolean organic, boolean severe, int upvote, String garbage, Button approved, Button disapproved) {
		this.name = name;
		this.organic = organic;
		this.severe = severe;
		this.upvote = upvote;
		this.garbage = garbage;
		this.approved = approved;
		this.disapproved = disapproved;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getOrganic() {
		return organic;
	}

	public void setOrganic(boolean organic) {
		this.organic = organic;
	}

	public boolean getSevere() {
		return severe;
	}

	public void setSevere(boolean severe) {
		this.severe = severe;
	}

	public int getUpvote() {
		return upvote;
	}

	public void setUpvote(int upvote) {
		this.upvote = upvote;
	}

	public String getGarbage() {
		return garbage;
	}

	public void setGarbage(String garbage) {
		this.garbage = garbage;
	}

	public Button getApproved() {
		return approved;
	}

	public void setApproved(Button approved) {
		this.approved = approved;
	}

	public Button getDisapproved() {
		return disapproved;
	}

	public void setDisapproved(Button disapproved) {
		this.disapproved = disapproved;
	}
}
