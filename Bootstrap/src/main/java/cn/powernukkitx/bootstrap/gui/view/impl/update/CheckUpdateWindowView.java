package cn.powernukkitx.bootstrap.gui.view.impl.update;

import cn.powernukkitx.bootstrap.gui.controller.CheckUpdateWindowController;
import cn.powernukkitx.bootstrap.gui.controller.Controller;
import cn.powernukkitx.bootstrap.gui.model.keys.UpdateWindowDataKeys;
import cn.powernukkitx.bootstrap.gui.model.values.JarLocationsWarp;
import cn.powernukkitx.bootstrap.gui.model.values.JavaLocationsWarp;
import cn.powernukkitx.bootstrap.gui.model.values.LibLocationsWarp;
import cn.powernukkitx.bootstrap.gui.view.SwingView;
import cn.powernukkitx.bootstrap.gui.view.View;
import cn.powernukkitx.bootstrap.gui.view.keys.CheckUpdateWindowViewKey;
import cn.powernukkitx.bootstrap.info.locator.JarLocator;
import cn.powernukkitx.bootstrap.info.locator.JavaLocator;
import cn.powernukkitx.bootstrap.info.locator.LibsLocator;
import cn.powernukkitx.bootstrap.info.locator.Location;
import cn.powernukkitx.bootstrap.util.GitUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static cn.powernukkitx.bootstrap.gui.view.impl.update.TreeEntry.*;
import static cn.powernukkitx.bootstrap.util.LanguageUtils.tr;
import static cn.powernukkitx.bootstrap.util.SwingUtils.getIcon;

public final class CheckUpdateWindowView extends JFrame implements SwingView<JFrame> {
    private final int viewID = View.newViewID();
    private final CheckUpdateWindowController controller;

    public CheckUpdateWindowView(CheckUpdateWindowController controller) {
        this.controller = controller;
    }

    @Override
    public CheckUpdateWindowViewKey getViewKey() {
        return CheckUpdateWindowViewKey.KEY;
    }

    @Override
    public int getViewID() {
        return viewID;
    }

    @Override
    public void init() {
        bind(UpdateWindowDataKeys.ICON, Image.class, this::setIconImage);
        bind(UpdateWindowDataKeys.TITLE, String.class, this::setTitle);
        bind(UpdateWindowDataKeys.WINDOW_SIZE, Dimension.class, this::setSize);
        bind(UpdateWindowDataKeys.DISPLAY, Boolean.class, this::setVisible);

        /* 添加组件 */
        final JScrollPane scrollPane = new JScrollPane();
        this.setContentPane(scrollPane);
        bind(UpdateWindowDataKeys.WINDOW_SIZE, Dimension.class, scrollPane::setPreferredSize);
        final JTree tree = new JTree();
        scrollPane.setViewportView(tree);
        tree.setCellRenderer(new CheckUpdateTreeCellRenderer());
        // 菜单
        final JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        {  // 选项菜单
            final JMenu menu = new JMenu(tr("gui.menu.update-option"));
            menuBar.add(menu);
            final JMenuItem refreshOption = new JMenuItem(tr("gui.menu.update-option.refresh"));
            menu.add(refreshOption);
            refreshOption.addActionListener(e -> controller.onRefresh());
        }
        // 根节点
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        final TreeModel treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
        {  // JVM信息
            final TreeEntry javaEntry = createWaitEntry(tr("gui.update-window.java-runtime"));
            final DefaultMutableTreeNode javaNode = new DefaultMutableTreeNode(javaEntry);
            rootNode.add(javaNode);
            bind(UpdateWindowDataKeys.JAVA_LOCATIONS, JavaLocationsWarp.class, value -> {
                final List<Location<JavaLocator.JavaInfo>> javaLocations = value.get();
                if (javaLocations.size() == 0) {
                    javaEntry.setIcon(getIcon("error.png", TreeEntry.SIZE));
                    javaNode.removeAllChildren();
                    final TreeEntry tmpEntry = createErrorEntry(tr("gui.update-window.java-not-found"));
                    final DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpEntry);
                    javaNode.add(tmpNode);
                } else {
                    List<Location<JavaLocator.JavaInfo>> get = value.get();
                    javaEntry.setIcon(getIcon("ok.png", TreeEntry.SIZE));
                    javaNode.removeAllChildren();
                    for (int i = 0, getSize = get.size(); i < getSize; i++) {
                        final Location<JavaLocator.JavaInfo> javaInfoLocation = get.get(i);
                        final JavaLocator.JavaInfo info = javaInfoLocation.getInfo();
                        if (i == 0) {
                            final TreeEntry tmpEntry = "17".equals(info.getMajorVersion()) ?
                                    createOkEntry(info.getFullVersion() + " - " + info.getVendor()) :
                                    createWarnEntry(info.getFullVersion() + " - " + info.getVendor());
                            final DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpEntry);
                            javaNode.add(tmpNode);
                            if (!"17".equals(info.getMajorVersion())) {
                                javaEntry.setIcon(getIcon("error.png", TreeEntry.SIZE));
                            }
                        } else {
                            final TreeEntry tmpEntry = "17".equals(info.getMajorVersion()) ?
                                    createComponentEntry(info.getFullVersion() + " - " + info.getVendor()) :
                                    createWarnEntry(info.getFullVersion() + " - " + info.getVendor());
                            final DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpEntry);
                            javaNode.add(tmpNode);
                        }
                    }
                }
                tree.repaint();
            });
        }
        {  // PNX信息
            final TreeEntry pnxEntry = createWaitEntry(tr("gui.update-window.pnx"));
            final DefaultMutableTreeNode pnxNode = new DefaultMutableTreeNode(pnxEntry);
            rootNode.add(pnxNode);
            bind(UpdateWindowDataKeys.PNX_LOCATIONS, JarLocationsWarp.class, value -> {
                final List<Location<JarLocator.JarInfo>> pnxLocations = value.get();
                if (pnxLocations.size() == 0) {
                    pnxEntry.setIcon(getIcon("error.png", TreeEntry.SIZE));
                    pnxNode.removeAllChildren();
                    final TreeEntry tmpEntry = createErrorEntry(tr("gui.update-window.pnx-not-found"));
                    final DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpEntry);
                    pnxNode.add(tmpNode);
                } else {
                    pnxEntry.setIcon(getIcon("ok.png", TreeEntry.SIZE));
                    pnxNode.removeAllChildren();
                    boolean conflict = false;
                    if (pnxLocations.size() != 1) {
                        conflict = true;
                        pnxEntry.setIcon(getIcon("error.png", TreeEntry.SIZE));
                        final TreeEntry tmpEntry = createErrorEntry(tr("gui.update-window.pnx-multi-conflict"));
                        final DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpEntry);
                        pnxNode.add(tmpNode);
                    }
                    for (final Location<JarLocator.JarInfo> pnxLocation : pnxLocations) {
                        final JarLocator.JarInfo info = pnxLocation.getInfo();
                        if (info.getGitInfo().isPresent()) {
                            final GitUtils.FullGitInfo gitInfo = info.getGitInfo().get();
                            final TreeEntry tmpEntry = conflict ?
                                    createWarnEntry(gitInfo.getMainVersion() + "-git-" + gitInfo.getCommitID()) :
                                    createOkEntry(gitInfo.getMainVersion() + "-git-" + gitInfo.getCommitID());
                            final DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(tmpEntry);
                            pnxNode.add(tmpNode);
                        }
                    }
                }
                tree.repaint();
            });
        }
        {  // 依赖库信息
            final TreeEntry libsEntry = createWaitEntry(tr("gui.update-window.libs"));
            final DefaultMutableTreeNode libsNode = new DefaultMutableTreeNode(libsEntry);
            rootNode.add(libsNode);
            bind(UpdateWindowDataKeys.LIBS_LOCATIONS, LibLocationsWarp.class, value -> {
                final List<Location<LibsLocator.LibInfo>> libLocations = value.get();
                boolean libFull = true;
                libsNode.removeAllChildren();
                for (final Location<LibsLocator.LibInfo> location : libLocations) {
                    final LibsLocator.LibInfo info = location.getInfo();
                    DefaultMutableTreeNode tmpNode;
                    if (!info.isExists()) {
                        final TreeEntry tmpEntry = createErrorEntry(info.getName());
                        tmpNode = new DefaultMutableTreeNode(tmpEntry);
                        libFull = false;
                    } else if (info.isNeedsUpdate()) {
                        final TreeEntry tmpEntry = createWarnEntry(info.getName());
                        tmpNode = new DefaultMutableTreeNode(tmpEntry);
                        libFull = false;
                    } else {
                        final TreeEntry treeEntry = createOkEntry(info.getName());
                        tmpNode = new DefaultMutableTreeNode(treeEntry);
                    }
                    libsNode.add(tmpNode);
                }
                if (!libFull) {
                    libsEntry.setIcon(getIcon("error.png", TreeEntry.SIZE));
                } else {
                    libsEntry.setIcon(getIcon("ok.png", TreeEntry.SIZE));
                }
                tree.repaint();
            });
        }
        {  // 根节点隐藏
            tree.setRootVisible(true);
            tree.expandRow(0);
            tree.setRootVisible(false);
        }
//        final JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
//        scrollPane.setViewportView(outerPanel);
//        // JVM信息
//        {
//            final JPanel panel = new JPanel();
//            final BoxLayout lo = new BoxLayout(panel, Y_AXIS);
//            panel.setLayout(lo);
//            outerPanel.add(panel);
//            final JLabel javaLabel = new JLabel(getIcon("wait.png", 16));
//            panel.add(warp(javaLabel, new JLabel(tr("gui.update-window.java-runtime"))));
//            bind(UpdateWindowDataKeys.JAVA_LOCATIONS, JavaLocationsWarp.class, value -> {
//                final List<Location<JavaLocator.JavaInfo>> javaLocations = value.get();
//                if(javaLocations.size() != 0) {
//                    javaLabel.setIcon(getIcon("error.png", 16));
//                    panel.add(warp(20, new JLabel(getIcon("error.png", 14)),
//                            warn(p(tr("gui.update-window.java-not-found")))));
//                }
//            });
//        }
        /* 初始化swing界面 */
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // 居中显示
        final Point pointScreenCenter = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        this.setLocation(pointScreenCenter.x - 240, pointScreenCenter.y - 160);
        // 监听窗口变化并反馈给控制器
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { // 监听重置大小
                controller.onResize(e.getComponent().getSize());
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.onClose();
            }
        });
    }

    @Override
    public JFrame getActualComponent() {
        return this;
    }

    @Override
    public Controller getController() {
        return controller;
    }
}
