package com.project.sharedfolderclient.v1.gui;

import com.project.sharedfolderclient.v1.file.FileDto;
import com.project.sharedfolderclient.v1.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Application gui class
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class MainFrame extends JFrame  {
    private final static Object[] columnNames = {"File name", "Kind", "Size", "Added At", "Last Modified"};
    private final static boolean[] editableCells = new boolean[]{
            true, false, false, false, false
    };
    private final static JLabel console = new JLabel("");
    private final FileService fileService;
    private DefaultTableModel fileModel;



    /**
     * Create the frame.
     */
    @PostConstruct
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
        JPanel southButtons = new JPanel();
        southButtons.setLayout(new GridLayout(1, 2));
        southButtons.add(createUploadButton());
        southButtons.add(createRefreshButton());
        contentPane.add(southButtons, BorderLayout.SOUTH);
        contentPane.add(console, BorderLayout.NORTH);

        log.debug("Done settings up the main frame");
    }

    /**
     *  print messages to status bar in red color
     */
    public void printError(String message) {
        console.setForeground(Color.RED);
        console.setText(message);
    }

    /**
     *  print messages to status bar in blue color
     */
    public void printSuccess(String message) {
        console.setForeground(Color.BLUE);
        console.setText(message);
    }


    /* helpers methods */

    private JButton createRefreshButton() {
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(evt -> refreshView());
        return refreshButton;
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
            JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            j.setCurrentDirectory(new java.io.File("."));
            j.setDialogTitle("Choose File to Upload");
            j.setFileSelectionMode(JFileChooser.FILES_ONLY);
            // invoke the showsSaveDialog function to show the save dialog
            if (j.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                // set the label to the path of the selected file
                String path = j.getSelectedFile().getAbsolutePath();
                File fileToUpload = new File(path);
                if (fileService.upload(fileToUpload) == null) {
                    return;
                }
                refreshView();
                printSuccess(String.format("File %s successfully uploaded",fileToUpload.getName()));

            }
        });
        return uploadButton;
    }

    private JTable createTableView() {
        JTable fileTable = new JTable() {
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                String newFileName = (String) aValue;
                String oldFileName = (String) getValueAt(row, column);
                if (StringUtils.equals(oldFileName,newFileName)) {
                    return;
                }
               FileDto renamedFile = fileService.rename(oldFileName, newFileName);
               if (renamedFile == null) {
                   return;
               }
               refreshView();
               printSuccess(String.format("file name %s was renamed to %s", oldFileName, newFileName));
            }
        };
        fileModel.setColumnIdentifiers(columnNames);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setFont(new Font("Tahoma", Font.PLAIN, 17));
        fileTable.setRowHeight(30);
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
                if (SwingUtilities.isRightMouseButton(e) && e.getComponent() instanceof JTable) {
                    JPopupMenu popup = createRightClickMenu(fileTable);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        return fileTable;
    }


    /* Pop up menu for mouse right click */
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
                String fileName = (String) fileTable.getModel().getValueAt( fileTable.getSelectedRow(),0);
                if (!fileService.deleteByName(fileName)) {
                    return;
                }
                printSuccess(String.format("file %s was deleted",fileName));
                refreshView();
            }
        });
        downloadItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent ev) {
                String fileNameToDownload = (String) fileTable.getModel().getValueAt(fileTable.getSelectedRow(), 0);
                JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                j.setFileSystemView(FileSystemView.getFileSystemView());
                j.setDialogTitle("Choose Folder");
                j.setApproveButtonText("Save");
                j.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "only directories can be selected";
                    }
                });
                j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                j.setAcceptAllFileFilterUsed(false);

                // invoke the showsSaveDialog function to show the open dialog
                if (j.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = j.getSelectedFile();
                    if (!selectedFile.isDirectory()) {
                        // fix an issue with macOS which select file instead of directory
                        selectedFile = selectedFile.getParentFile();
                    }
                    // set the label to the path of the selected file
                    String path = selectedFile.getAbsolutePath();
                    try {
                        if (fileService.download(fileNameToDownload, path) == null) {
                            return;
                        }
                        printSuccess("file " + fileTable.getModel().getValueAt(fileTable.getSelectedRow(), 0)
                                + " was downloaded to " + path);
                    } catch (Exception e) {
                        log.error("Could not download the file to {}: {}", path, e.getMessage());
                        printError(String.format("Could not download the file to %s: %s",path, e.getMessage()));
                    }
                }
            }
        });
        return rightClickPopupMenu;
    }

    /**
     *  call the remote server and retrieving the latest file list
     *  shows it in the main frame
    **/
    private void refreshView() {
        List<FileDto> fileList = fileService.list();
        fileModel.setRowCount(0);
        if (CollectionUtils.isEmpty(fileList)) {
            return;
        }
        fileList
                .forEach(file-> {
            Object[] fileRow = {file.getName(), file.getKind(), file.getSize(), file.getDateAdded(), file.getDateModified()};
            fileModel.addRow(fileRow);
        });
        printSuccess("File list updated at: " + new Date(System.currentTimeMillis()));
    }

    /**
     * when application ready,
     * making a request to get the file list and refreshing the view
     */
    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStart() {
        refreshView();
    }
}

