package am.ui.glue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import am.extension.batchmode.simpleBatchMode.SimpleBatchModeRunner;
import am.matcher.lod.LinkedOpenData.LODBatch;
import am.matcher.oaei.imei2013.InstanceMatching;
import am.ui.UICore;
import am.ui.UIMenu;
import am.ui.api.AMMenuItem;

public class LODMenuItem extends JMenu implements AMMenuItem {

	private static final long serialVersionUID = 4214490219241892299L;

	@Override public String getMenuLocation() { return UIMenu.MENU_MATCHERS + "/"; }
	@Override public JMenuItem getMenuItem() { return this; }
	

	public LODMenuItem() {
		super("Linked Open Data");
		
		JMenuItem runBatchLOD = new JMenuItem("Run LOD Schema Matching (old)");
		
		runBatchLOD.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				LODMenuItem.this.runLODBatchOld();
			}
		});
		
		add(runBatchLOD);
		
		JMenuItem runBatchLODnew = new JMenuItem("Run LOD Schema Matching");
		runBatchLODnew.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				LODMenuItem.this.runLODBatch();
			}
		});
		
		add(runBatchLODnew);
		
		addSeparator();
		
		JMenuItem runIMEI2013_01 = new JMenuItem("IMEI 2013");
		runIMEI2013_01.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				LODMenuItem.this.runIMEI2013();
			}
		});
		
		add(runIMEI2013_01);
	}
	
	private void runLODBatch() {
		Runnable lod = new Runnable() {
			@Override public void run() {
				try {
					LODBatch batch = new LODBatch();
					batch.run();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							UICore.getUI().getUIFrame(), 
							e.getClass() + "\n" + e.getMessage(), 
							"ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		Thread lodThread = new Thread(lod);
		lodThread.setName("LODBatch " +  lodThread.getId());
		lodThread.start();
	}
	
	private void runLODBatchOld() {
		Runnable lod = new Runnable() {
			@Override public void run() {
				try {
					LODBatch batch = new LODBatch();
					batch.runOldVersion();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							UICore.getUI().getUIFrame(), 
							e.getClass() + "\n" + e.getMessage(), 
							"ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		Thread lodThread = new Thread(lod);
		lodThread.setName("LODBatch " +  lodThread.getId());
		lodThread.start();
	}
	
	private void runIMEI2013() {
		Runnable imei2013_01 = new Runnable() {
			@Override public void run() {
				try {
					InstanceMatching im = new InstanceMatching();
					im.runTest01();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							UICore.getUI().getUIFrame(), 
							e.getClass() + "\n" + e.getMessage(), 
							"ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		Thread imei2013_01Thread = new Thread(imei2013_01);
		imei2013_01Thread.setName("IMEI2013 01 " +  imei2013_01Thread.getId());
		imei2013_01Thread.start();
	}
}
