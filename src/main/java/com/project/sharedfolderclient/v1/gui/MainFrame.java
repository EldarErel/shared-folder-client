package com.project.sharedfolderclient.v1.gui;

import com.project.sharedfolderclient.v1.exception.ApplicationEvents;
import com.project.sharedfolderclient.v1.exception.BaseError;
import com.project.sharedfolderclient.v1.sharedfile.SharedFile;
import com.project.sharedfolderclient.v1.sharedfolder.SharedFolderService;
import com.project.sharedfolderclient.v1.utils.error.Error;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

@Component
@Slf4j
public class MainFrame extends JFrame {
    private DefaultTableModel fileModel;
    private final JLabel console = new JLabel("");
    private final static Object[] columnNames = {"File name", "Kind ", "Size", "Added At", "Last Modified"};
    private final static boolean[] editableCells = new boolean[]{
            true, false, false, false, false
    };
    @Autowired
    private SharedFolderService sharedFolderService;

    /**
     * Create the frame.
     */
    public void init() {
        log.info("Starting application");
        setTitle("Shared Folder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1080, 768);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setFont(new Font("Tahoma", Font.PLAIN, 17));
        setContentPane(contentPane);
        log.debug("Creating the table and view");
        fileModel = createTableModel();
        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(createTableView());
        contentPane.add(createUploadButton(), BorderLayout.SOUTH);
        contentPane.add(console, BorderLayout.NORTH);
        log.debug("Retrieving list of files");
        refreshView();
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel() {
            final boolean[] canEdit = editableCells;

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };
    }

    private JButton createUploadButton() {
        JButton uploadButton = new JButton("Upload file");
        uploadButton.addActionListener(evt -> {
            // create an object of JFileChooser class
            JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            // invoke the showsSaveDialog function to show the save dialog
            int r = j.showSaveDialog(null);
            // if the user selects a file
            if (r == JFileChooser.APPROVE_OPTION) {
                // set the label to the path of the selected file
                String path = j.getSelectedFile().getAbsolutePath();
                File fileToUpload = new File(path);
                if (sharedFolderService.upload(fileToUpload) == null) {
                    return;
                }
                printSuccess("File successfully uploaded");
                refreshView();

            }
        });
        return uploadButton;
    }

    private void printError(String message) {
        console.setForeground(Color.RED);
        console.setText(message);
    }

    private void printSuccess(String message) {
        console.setForeground(Color.BLUE);
        console.setText(message);
    }

    private JTable createTableView() {
        JTable fileTable = new JTable();
        fileModel.setColumnIdentifiers(columnNames);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setFont(new Font("Tahoma", Font.PLAIN, 17));
        fileTable.setModel(fileModel);
        fileTable.setBackground(Color.white);
        fileTable.setAutoCreateRowSorter(true);
        fileTable.setRowSelectionAllowed(true);
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = fileTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < fileTable.getRowCount()) {
                    fileTable.setRowSelectionInterval(r, r);
                } else {
                    fileTable.clearSelection();
                }
                int rowindex = fileTable.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    JPopupMenu popup = createRightClickMenu(fileTable);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }

            }
        });
        ;
        return fileTable;
    }

    private JPopupMenu createRightClickMenu(JTable fileTable) {
        JPopupMenu rightClickPopupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem downloadItem = new JMenuItem("Download");
        rightClickPopupMenu.add(downloadItem);
        rightClickPopupMenu.add(deleteItem);
        deleteItem.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            @Override
            public void mouseReleased(MouseEvent e) {
                String fileName = (String) fileTable.getModel().getValueAt(0, fileTable.getSelectedRow());
                if (!sharedFolderService.deleteByName(fileName)) {
                    return;
                }
                refreshView();
                printSuccess("file " + fileTable.getModel().getValueAt(0, fileTable.getSelectedRow()) + " was deleted");
            }
        });
        downloadItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent ev) {
                String fileName = (String) fileTable.getModel().getValueAt(0, fileTable.getSelectedRow());
                // create an object of JFileChooser class
                JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                // invoke the showsSaveDialog function to show the save dialog
                int r = j.showSaveDialog(null);
                // if the user selects a file
                if (r == JFileChooser.APPROVE_OPTION) {
                    // set the label to the path of the selected file
                    String path = j.getSelectedFile().getAbsolutePath();
                    String fileNameToDownload = (String) fileTable.getModel().getValueAt(0, fileTable.getSelectedRow());
                    try {
                        if (sharedFolderService.download(fileNameToDownload, path) == null) {
                            return;
                        }
                        printSuccess("file " + fileTable.getModel().getValueAt(0, fileTable.getSelectedRow())
                                + " was downloaded to " + path);
                    } catch (Exception e) {
                        log.error("Could not download the file {}", e.getMessage());
                        printError("Could not download the file: " + e.getMessage());
                    }
                }
            }
        });
        return rightClickPopupMenu;
    }

    private void refreshView() {
        Object[] fileList = Optional.ofNullable(sharedFolderService.list())
                .orElse(new ArrayList<>())
                .stream()
                .map(file -> (Object) file)
                .toArray();
        for (Object fileAsObject : fileList) {
            SharedFile pointer = (SharedFile) fileAsObject;
            Object[] fileRow = {pointer.getName(), pointer.getKind(), pointer.getSize(), pointer.getDateAdded(), pointer.getDateModified()};
            fileModel.addRow(fileRow);
        }
    }

    @EventListener
    public void errorHandler(ApplicationEvents.BaseErrorEvent errorEvent) {
        BaseError error = (BaseError) errorEvent.getSource();
        printError(error.getMessage());
    }

    @EventListener
    public void errorHandler(ApplicationEvents.ErrorEvent errorEvent) {
        Error error = (Error) errorEvent.getSource();
        printError(error.getName() + ": " + error.getMessage());
    }
}

