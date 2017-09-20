package edu.rosehulman.p2p.impl.notification;

import edu.rosehulman.p2p.protocol.IHost;

public interface IFoundListener {

	void activityPerformed(String fileName, IHost foundAt);

}
