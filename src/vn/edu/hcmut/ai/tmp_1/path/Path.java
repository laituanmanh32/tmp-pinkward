package vn.edu.hcmut.ai.tmp_1.path;

import java.util.ArrayList;

import vn.edu.hcmut.ai.tmp_1.utils.Util;

public class Path {
	private final int MAX_SIZE = 5000;
	private String name;
	private ArrayList<PathNode> list;

	public Path(String name) {
		this.name = name;
		list = new ArrayList<PathNode>();
	}

	// ---------------------------------------------------------------
	public void addNode(PathNode node) {

		if (getSize() > 0) {
			PathNode topNode = getNode();
			long disTime = node.time - topNode.time;
			if (disTime > 1 && disTime <= 16) { // fill up lost nodes
				double enemyDistance = Util.distance(topNode.enemyX,
						topNode.enemyY, node.enemyX, node.enemyY);
				double enemyVelocity = enemyDistance / disTime;
				double enemyMoveDir = Math.atan2(node.enemyX - topNode.enemyX,
						node.enemyY - topNode.enemyY);
				double enemyHeading = enemyMoveDir;
				if (topNode.enemyVelocity < 0)
					enemyHeading += Math.PI;
				enemyHeading = Util.standardAngle(enemyHeading);

				double myDistance = Util.distance(topNode.myX, topNode.myY,
						node.myX, node.myY);
				double myVelocity = myDistance / disTime;
				double myMoveDir = Math.atan2(node.myX - topNode.myX, node.myY
						- topNode.myY);
				double myHeading = myMoveDir;
				if (topNode.myVelocity < 0)
					myHeading += Math.PI;
				myHeading = Util.standardAngle(myHeading);
				for (int i = 1; i < disTime; i++) {
					PathNode fillNode = new PathNode();
					fillNode.time = topNode.time + i;
					fillNode.round = topNode.round;
					fillNode.enemyEnergy = topNode.enemyEnergy;
					fillNode.myEnergy = topNode.myEnergy;

					fillNode.myX = topNode.myX + Math.sin(myMoveDir)
							* myVelocity * i;
					fillNode.myY = topNode.myY + Math.cos(myMoveDir)
							* myVelocity * i;
					fillNode.myVelocity = myVelocity;
					fillNode.myHeading = myHeading;

					fillNode.enemyX = topNode.enemyX + Math.sin(enemyMoveDir)
							* enemyVelocity * i;
					fillNode.enemyY = topNode.enemyY + Math.cos(enemyMoveDir)
							* enemyVelocity * i;
					fillNode.enemyVelocity = enemyVelocity;
					fillNode.enemyHeading = enemyHeading;

					list.add(fillNode);
				}
			}
		}
		list.add(node);
		while (list.size() >= MAX_SIZE)
			list.remove(0);
	}

	public PathNode getNode(int index) {
		if (index < 0 || index >= list.size())
			return null;
		else
			return (PathNode) list.get(index);
	}

	public PathNode getNode() {
		if (list.size() == 0)
			return null;
		else
			return (PathNode) list.get(list.size() - 1);
	}

	public int getSize() {
		return list.size();
	}

	// ------------------------------------------------------------------
	public void onFire(double power) {
		PathNode node = getNode();
		if (node != null)
			node.myFire = power;
	}

	public void onBulletHit(double power) {
		PathNode node = getNode();
		if (node != null)
			node.myBulletHit = power;
	}

	public void onEnemyFire(double power) {
		PathNode node = getNode();
		if (node != null)
			node.enemyFire = power;
	}

	public void onEnemyBulletHit(double power) {
		PathNode node = getNode();
		if (node != null)
			node.enemyBulletHit = power;
	}

	// --------------------------------------------------------------------
	public String getName() {
		return name;
	}
}
