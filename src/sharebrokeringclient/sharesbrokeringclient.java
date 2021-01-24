/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sharebrokeringclient;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.me.stockservice.DatatypeConfigurationException_Exception;
import org.me.stockservice.ParseExceptionException;
import org.netbeans.xml.schema.share.Stock;

/**
 *
 * @author Jack
 */
public final class sharesbrokeringclient extends javax.swing.JFrame {

    public sharesbrokeringclient() throws DatatypeConfigurationException_Exception, ParseExceptionException, IOException {
        FileHandler fileHandler = new FileHandler("C:\\Users\\Jack\\Documents\\NetBeansProjects\\ShareBrokeringClient\\money.txt", "C:\\Users\\Jack\\Documents\\NetBeansProjects\\ShareBrokeringClient\\shares.txt");
        this.fileHandler = fileHandler;
        
        initComponents(); // Generated code stuff
        
        initComboBoxes(); // Populate combo boxes
        loadTable(); // Populate table with stock data
        initTableRowSorter(); // Create table row sorter
        initMoney(); // Load money value from file
        initShares(); //Load shares from value
        
        // Set window title
        JTextField myTitle;
        myTitle = new JTextField("Shares Brokering Client");  
        this.setTitle(myTitle.getText());  
    }
    
    private void initMoney() {
        jLabel8.setText(fileHandler.readMoney());
    }
    
    private void subtractMoney(double money) {
        //double newValue = Double.parseDouble(jLabel8.getText()) - money;
        double newValue = Double.parseDouble(fileHandler.readMoney()) - money;
        
        fileHandler.writeMoney(newValue);
        jLabel8.setText(fileHandler.readMoney());
    }
    
    private void addMoney(double money) {
        double newValue = Double.parseDouble(fileHandler.readMoney()) + money;
        
        fileHandler.writeMoney(newValue);
        jLabel8.setText(fileHandler.readMoney());
    }
    
    private void initShares() throws FileNotFoundException {
        ArrayList<String> shares = fileHandler.readShares();
    
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        
        clearTable2();
                
        for (int x = 0; x < shares.size(); x++) {
            String[] splitStr = shares.get(x).split(",");
            model.addRow(new Object[]{splitStr[0].trim(), splitStr[1].trim()});
        }
    }
    
    private void subtractShares(String sellStockName, int sellQuantity, int index) {
        try {
            ArrayList<String> shares = fileHandler.readShares();
            
            String[] splitStr = shares.get(index).split(",");
            
            if ((Integer.parseInt(splitStr[1].trim()) - sellQuantity) == 0) {
                shares.remove(index);
            } else {
                shares.set(index, sellStockName + ", " + (Integer.parseInt(splitStr[1].trim()) - sellQuantity));
            }
            
            fileHandler.writeShares(shares);
            initShares();
        } catch (FileNotFoundException ex) {
        }
    }
    
    private void addShares(String buyStockName, int buyQuantity) {
        boolean existingStock = false;
        
        try {
            ArrayList<String> shares = fileHandler.readShares();
            
            for (int x = 0; x < shares.size(); x++) {
                String[] splitStr = shares.get(x).split(",");
                if (splitStr[0].trim().equals(buyStockName)) {
                    shares.set(x, buyStockName + ", " + (Integer.parseInt(splitStr[1].trim()) + buyQuantity));
                    existingStock = true;
                }
            }
            
            if (!existingStock) {
                shares.add(buyStockName + ", " + buyQuantity);
            }
            
            fileHandler.writeShares(shares);
            initShares();
        } catch (FileNotFoundException ex) {
        }
    }
    
    // HTTP GET request
    private String queryRESTSerivce(String symbol, String type) throws IOException {
        URL obj = new URL("http://localhost:53981/RESTStockService/webresources/queryAPI?symbol=" + symbol + "&type=" + type);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
        String inputLine;
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            System.out.println("GET request successful");
            
            return response.toString();
        } else {
                System.out.println("GET request failed");
                return "Failed to contact REST service";
        }
    }

    // Populate table with stock data
    public void loadTable() throws DatatypeConfigurationException_Exception, ParseExceptionException {
        List<Stock> myStocks = getStocks(selectedCurrency, "", "");
        
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        
        for (int x = 0; x < myStocks.size(); x++) {
            String lastUpdated = myStocks.get(x).getPrice().getLastUpdateDate().toString();
            String[] splitStr = lastUpdated.split("T");
            
            if ((splitStr[1].substring(splitStr[1].length() - 1)).equals("Z")) {
                splitStr[1] = splitStr[1].substring(0, splitStr[1].length() - 1);
            }
            
            try {
                model.addRow(new Object[]{myStocks.get(x).getCompanyName(), myStocks.get(x).getCompanySymbol(), myStocks.get(x).getNoAvailableShares(), myStocks.get(x).getPrice().getValue(), queryRESTSerivce(myStocks.get(x).getCompanySymbol(), "adj_open"), queryRESTSerivce(myStocks.get(x).getCompanySymbol(), "adj_close"), myStocks.get(x).getPrice().getCurrency(), splitStr[1], splitStr[0]});
            } catch (IOException ex) {
                Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // Removes all data from table
    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
    }
    
     // Removes all data from table
    public void clearTable2() {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
    }
    
    // Create row sorter for table
    public void initTableRowSorter() {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(jTable1.getModel());
        jTable1.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
    }
    
    // Populate combo boxes with options
    public void initComboBoxes() throws DatatypeConfigurationException_Exception, ParseExceptionException {
        String[] searchTypes = {"Company:", "Symbol:", "Available shares greater than:", "Available shares less than:", "Price higher than:", "Price lower than:"};
        List<String> currencyCodes = getCurrencyCodes();
     
        INITIALIZED = false;
        
        jComboBox1.removeAllItems();
        jComboBox2.removeAllItems();
        jComboBox3.removeAllItems();
        jComboBox4.removeAllItems();
        jComboBox5.removeAllItems();
        jComboBox6.removeAllItems();
        
        // Load currency combobox options
        for (int x = 0; x < currencyCodes.size(); x++) {
            jComboBox3.addItem(currencyCodes.get(x));
            jComboBox6.addItem(currencyCodes.get(x));
            if (currencyCodes.get(x).equals("USD - United States Dollar")) {
                // Set default selected as USD
                jComboBox3.setSelectedIndex(x);
                jComboBox6.setSelectedIndex(x);
            }
        }
        
        // Set default as British pound
        selectedCurrency = jComboBox3.getSelectedItem().toString();
        
        // Load stock data with selected currency
        List<Stock> myStocks = getStocks(selectedCurrency, "", "");
        
        // Load buy/sell combobox stocks
        for (int x = 0; x < myStocks.size(); x++) {
            jComboBox1.addItem(myStocks.get(x).getCompanyName());
            jComboBox2.addItem(myStocks.get(x).getCompanyName());
            jComboBox5.addItem(myStocks.get(x).getCompanyName());
        }
        
        // Load search type combobox options
        for (String searchType : searchTypes) {
            jComboBox4.addItem(searchType);
        }
        
        INITIALIZED = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBox3 = new javax.swing.JComboBox<>();
        jButton5 = new javax.swing.JButton();
        jComboBox4 = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<>();
        jTextField2 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel23 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel16 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Company", "Symbol", "Available Shares", "Price", "Adjusted Open", "Adjusted Close", "Currency", "Time Last Updated", "Date Last Updated"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(125);
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(30);
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(50);
            jTable1.getColumnModel().getColumn(3).setPreferredWidth(30);
            jTable1.getColumnModel().getColumn(6).setPreferredWidth(150);
        }

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });

        jButton5.setText("Clear");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel9.setText("Select Currency:");

        jLabel10.setText("Search Criteria:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jTextField1.setForeground(java.awt.Color.lightGray);
        jTextField1.setText("E.g. '15'");
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField1FocusLost(evt);
            }
        });

        jButton2.setText("Buy Shares");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setText("Search");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTextField3.setForeground(java.awt.Color.lightGray);
        jTextField3.setText("E.g. 'intel'");
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField3FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField3FocusGained(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jTextField2.setForeground(java.awt.Color.lightGray);
        jTextField2.setText("E.g. '15'");
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField2FocusLost(evt);
            }
        });

        jButton3.setText("Sell Shares");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel12.setText("Select Stock to Buy:");

        jLabel13.setText("Select Stock to Sell:");

        jLabel14.setFont(new java.awt.Font("Microsoft YaHei UI Light", 1, 36)); // NOI18N
        jLabel14.setText("User Home");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(42, 42, 42)
                                .addComponent(jButton2))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(387, 387, 387)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(41, 41, 41)
                                .addComponent(jButton3))
                            .addComponent(jLabel13)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton4)
                                .addGap(18, 18, 18)
                                .addComponent(jButton5))
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 790, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3))))
                .addContainerGap(91, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Home", jPanel1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Share", "Volume"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jLabel23.setFont(new java.awt.Font("Microsoft YaHei UI Light", 1, 36)); // NOI18N
        jLabel23.setText("User Owned Shares");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(992, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel23)
                .addGap(30, 30, 30)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Your Shares", jPanel5);

        jLabel15.setFont(new java.awt.Font("Microsoft YaHei UI Light", 1, 36)); // NOI18N
        jLabel15.setText("Admin Features");

        jLabel16.setText("Admin Password:");

        jButton7.setText("Login");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jPanel3.setBackground(java.awt.Color.lightGray);
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setForeground(java.awt.Color.lightGray);
        jPanel3.setOpaque(false);

        jButton6.setText("Delete");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel11.setText("Select Stock to Delete:");

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel17.setFont(new java.awt.Font("Microsoft YaHei UI Light", 1, 24)); // NOI18N
        jLabel17.setForeground(java.awt.Color.red);
        jLabel17.setText("Delete Existing Stock");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6)
                    .addComponent(jLabel17))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setOpaque(false);

        jLabel4.setText("Number of Available Shares:");

        jTextField6.setForeground(java.awt.Color.lightGray);
        jTextField6.setText("E.g. 50");
        jTextField6.setToolTipText("");
        jTextField6.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField6FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField6FocusGained(evt);
            }
        });

        jLabel3.setText("Company Symbol:");

        jLabel2.setText("Company Name:");

        jTextField4.setForeground(java.awt.Color.lightGray);
        jTextField4.setText("E.g. Ford");
        jTextField4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField4FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField4FocusGained(evt);
            }
        });

        jTextField5.setForeground(java.awt.Color.lightGray);
        jTextField5.setText("E.g. FRD");
        jTextField5.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField5FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField5FocusGained(evt);
            }
        });

        jLabel5.setText("Currency:");

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Price:");

        jTextField8.setForeground(java.awt.Color.lightGray);
        jTextField8.setText("E.g. 5.50");
        jTextField8.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField8FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField8FocusGained(evt);
            }
        });

        jButton1.setText("Create");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Microsoft YaHei UI Light", 1, 24)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(0, 175, 0));
        jLabel18.setText("Create New Stock");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1)
                    .addComponent(jComboBox6, 0, 330, Short.MAX_VALUE)
                    .addComponent(jTextField6)
                    .addComponent(jTextField5)
                    .addComponent(jTextField4)
                    .addComponent(jTextField8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(148, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel18)
                    .addContainerGap(242, Short.MAX_VALUE)))
        );

        jLabel19.setForeground(java.awt.Color.red);
        jLabel19.setText("Logged Out");

        jPanel6.setBackground(java.awt.Color.lightGray);
        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setForeground(java.awt.Color.lightGray);
        jPanel6.setOpaque(false);

        jButton8.setText("Confirm");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel20.setText("Set Client Funds:");

        jLabel21.setFont(new java.awt.Font("Microsoft YaHei UI Light", 1, 24)); // NOI18N
        jLabel21.setForeground(java.awt.Color.red);
        jLabel21.setText("Set Funds");

        jTextField7.setForeground(java.awt.Color.lightGray);
        jTextField7.setText("E.g. 99.99");
        jTextField7.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField7FocusLost(evt);
            }
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField7FocusGained(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel20)
                    .addComponent(jButton8)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jButton8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 485, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton7)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel19)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel16)
                        .addComponent(jButton7)
                        .addComponent(jLabel19)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(241, 241, 241))
        );

        jTabbedPane1.addTab("Admin", jPanel2);

        jLabel1.setFont(new java.awt.Font("Microsoft JhengHei UI Light", 1, 36)); // NOI18N
        jLabel1.setText("Shares Brokering Service");

        jLabel7.setFont(new java.awt.Font("Microsoft JhengHei UI Light", 1, 36)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Microsoft JhengHei UI Light", 1, 36)); // NOI18N
        jLabel8.setText("jLabel8");

        jLabel22.setFont(new java.awt.Font("Microsoft JhengHei UI Light", 1, 36)); // NOI18N
        jLabel22.setText("$");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(305, 305, 305)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // "Buy shares" button
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int buyResult;
        int buyQuantity;
        String buyStockName;
        int priceIndex = 0;
        
        buyQuantity = Integer.parseInt(jTextField1.getText());
        buyStockName = jComboBox1.getSelectedItem().toString();
        
        for (int x = 0; x < jTable1.getRowCount(); x++) {
            if (buyStockName.equals(jTable1.getModel().getValueAt(x, 0))) {
                priceIndex = x;
                break;
            }
        }

        double stockPrice = Double.valueOf(jTable1.getModel().getValueAt(priceIndex, 3).toString());
        double stockValue = buyQuantity * stockPrice;
                
        try {
            if (stockValue > Double.parseDouble(fileHandler.readMoney())) {
                showMessageDialog(null, "You don't have enough money!");
            } else {
                buyResult = buyStocks(buyQuantity, buyStockName);
            
                if (buyResult == 1) {
                    clearTable();
                    loadTable();
                    showMessageDialog(null, "Success!");
                    subtractMoney(stockValue);
                    addShares(buyStockName, buyQuantity);
                } else {
                    showMessageDialog(null, "Failed!");
                }
            }
        } catch (NumberFormatException e) {
            showMessageDialog(null, "Please enter a quantity to buy");
        } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
            Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    // "Sell shares" button
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int sellResult;
        int sellQuantity;
        String sellStockName;
        int sharesIndex = 0;
        int priceIndex = 0;
        boolean validSale = false;
        
        sellQuantity = Integer.parseInt(jTextField2.getText());
        sellStockName = jComboBox2.getSelectedItem().toString();
        
        for (int x = 0; x < jTable2.getRowCount(); x++) {
            if (sellStockName.equals(jTable2.getModel().getValueAt(x, 0))) { // Does the client have any of that stock?
                if (sellQuantity <= Integer.parseInt(jTable2.getModel().getValueAt(x, 1).toString())) { // Is the client trying to sell more than they have?
                    validSale = true;
                    sharesIndex = x;
                }
            }
        }
        
        if (validSale) {
            for (int y = 0; y < jTable1.getRowCount(); y++) {
                if (sellStockName.equals(jTable1.getModel().getValueAt(y, 0))) {
                    priceIndex = y;
                    break;
                }
            }

            double stockPrice = Double.valueOf(jTable1.getModel().getValueAt(priceIndex, 3).toString());
            double stockValue = sellQuantity * stockPrice;

            try {
                sellResult = sellStocks(sellQuantity, sellStockName);

                if (sellResult == 1) {
                    clearTable();
                    loadTable();
                    showMessageDialog(null, "Success!");
                    addMoney(stockValue);
                    subtractShares(sellStockName, sellQuantity, sharesIndex);
                } else {
                    showMessageDialog(null, "Failed!");
                }
            } catch (NumberFormatException e) {
                showMessageDialog(null, "Please enter a quantity to sell");
            } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
                Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            showMessageDialog(null, "You do not have enough of this stock for this transaction!");
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    // "Search" button
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        try {
            loadSearchedTable();
        } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
            Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    // Populate table with searhed stock data
    private void loadSearchedTable() throws DatatypeConfigurationException_Exception, ParseExceptionException {
        String searchString = jTextField3.getText();
        String searchType = jComboBox4.getSelectedItem().toString();
        
        List<Stock> searchResults = getStocks(selectedCurrency, searchString, searchType);
        
        if (searchResults.size() > 0) {
            clearTable();
            
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        
            for (int x = 0; x < searchResults.size(); x++) {
                String lastUpdated = searchResults.get(x).getPrice().getLastUpdateDate().toString();
                String[] splitStr = lastUpdated.split("T");

                if ((splitStr[1].substring(splitStr[1].length() - 1)).equals("Z")) {
                    splitStr[1] = splitStr[1].substring(0, splitStr[1].length() - 1);
                }
                
                try {
                    model.addRow(new Object[]{searchResults.get(x).getCompanyName(), searchResults.get(x).getCompanySymbol(), searchResults.get(x).getNoAvailableShares(), searchResults.get(x).getPrice().getValue(), queryRESTSerivce(searchResults.get(x).getCompanySymbol(), "adj_open"), queryRESTSerivce(searchResults.get(x).getCompanySymbol(), "adj_close"), searchResults.get(x).getPrice().getCurrency(), splitStr[1], splitStr[0]});
                } catch (IOException ex) {
                    Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            showMessageDialog(null, "No results found");
        }
    }
    
    // "Clear" button
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        clearTable();
        jTextField3.setText("");
        try {
            loadTable();
        } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
            Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    // Select preferred stock currency
    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        if (INITIALIZED) {
            selectedCurrency = jComboBox3.getSelectedItem().toString();

            if (!jTextField3.getText().equals("E.g. 'intel'")) {
                try {
                    loadSearchedTable();
                } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
                    Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                clearTable();
                try {
                    loadTable();
                } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
                    Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println(selectedCurrency);
        }
    }//GEN-LAST:event_jComboBox3ActionPerformed

    // Send create stock request to brokering service
    private void sendCreateStockRequest(String newCompanyName, String newCompanySymbol, int newCompanyAvailableShares, String newCompanyCurrency, BigDecimal newCompanyPriceBD) throws DatatypeConfigurationException_Exception, ParseExceptionException {
        String result = addStock(newCompanyName, newCompanySymbol, newCompanyAvailableShares, newCompanyCurrency, newCompanyPriceBD);

        showMessageDialog(null, result);

        clearTable();
        loadTable();
        initComboBoxes();
    }
    
    private void jTextField3FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField3FocusGained
        jTextField3.setText("");
        jTextField3.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField3FocusGained

    private void jTextField3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField3FocusLost
        if (jTextField3.getText().equals("")) {
            jTextField3.setText("E.g. 'intel'");
            jTextField3.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField3FocusLost

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained
        jTextField1.setText("");
        jTextField1.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField1FocusGained

    private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
        if (jTextField1.getText().equals("")) {
            jTextField1.setText("E.g. '15'");
            jTextField1.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField1FocusLost

    private void jTextField2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusGained
        jTextField2.setText("");
        jTextField2.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField2FocusGained

    private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusLost
        if (jTextField2.getText().equals("")) {
            jTextField2.setText("E.g. '15'");
            jTextField2.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField2FocusLost

    // "Delete" button
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        String selectedStock = jComboBox5.getSelectedItem().toString();

        String result = removeStock(selectedStock);
        
        showMessageDialog(null, result);

        clearTable();
        try {
            loadTable();
            initComboBoxes();
        } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
            Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jTextField8FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField8FocusLost
        if (jTextField8.getText().equals("")) {
            jTextField8.setText("E.g. 5.50");
            jTextField8.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField8FocusLost

    private void jTextField8FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField8FocusGained
        jTextField8.setText("");
        jTextField8.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField8FocusGained

    private void jTextField6FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField6FocusLost
        if (jTextField6.getText().equals("")) {
            jTextField6.setText("E.g. 50");
            jTextField6.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField6FocusLost

    private void jTextField6FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField6FocusGained
        jTextField6.setText("");
        jTextField6.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField6FocusGained

    private void jTextField5FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField5FocusLost
        if (jTextField5.getText().equals("")) {
            jTextField5.setText("E.g. FRD");
            jTextField5.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField5FocusLost

    private void jTextField5FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField5FocusGained
        jTextField5.setText("");
        jTextField5.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField5FocusGained

    private void jTextField4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField4FocusLost
        if (jTextField4.getText().equals("")) {
            jTextField4.setText("E.g. Ford");
            jTextField4.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField4FocusLost

    private void jTextField4FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField4FocusGained
        jTextField4.setText("");
        jTextField4.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField4FocusGained

    // "Create" button
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int newCompanyAvailableShares = 0;
        double newCompanyPrice;
        BigDecimal newCompanyPriceBD = null;
        boolean inputValid = true;

        String newCompanyName = jTextField4.getText();
        String newCompanySymbol = jTextField5.getText().toUpperCase();
        
        String newCompanySelectedCurrency = jComboBox6.getSelectedItem().toString();
        String[] splitSelectedCurrencyString = newCompanySelectedCurrency.split("-");
        String newCompanyCurrency = splitSelectedCurrencyString[1].trim();

        // Validate new company symbol
        if (newCompanySymbol.length() > 5) {
            showMessageDialog(null, "Please enter less than 6 characters for the company symbol");
            inputValid = false;
        }

        // Validate new company available shares
        try {
            newCompanyAvailableShares = Integer.parseInt(jTextField6.getText());
        } catch (NumberFormatException e) {
            showMessageDialog(null, "Please enter a integer for available shares");
            inputValid = false;
        }

        // Validate new company price
        try {
            newCompanyPrice = Double.parseDouble(jTextField8.getText());
            newCompanyPriceBD = BigDecimal.valueOf(newCompanyPrice);
        } catch (NumberFormatException e) {
            showMessageDialog(null, "Please enter a price, format: 'X.Y'");
            inputValid = false;
        }

        if (inputValid) {
            try {
                sendCreateStockRequest(newCompanyName, newCompanySymbol, newCompanyAvailableShares, newCompanyCurrency, newCompanyPriceBD);
            } catch (DatatypeConfigurationException_Exception | ParseExceptionException ex) {
                Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // "Login" button
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        String adminPassword = "admin";
        String password = "";
        
        char[] passwordChars = jPasswordField1.getPassword();
        
        for (int x = 0; x < passwordChars.length; x++) {
            password = password + passwordChars[x];
        }
        
        if (password.equals(adminPassword)) {
            jButton1.setEnabled(true);
            jButton6.setEnabled(true);
            jButton8.setEnabled(true);
            jLabel19.setText("Logged In");
            jLabel19.setForeground(Color.GREEN);
        } else {
            showMessageDialog(null, "Incorrect password!");
        }
        
        jPasswordField1.setText("");
    }//GEN-LAST:event_jButton7ActionPerformed

    // When tabs are changed
    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        
        // Logout admin when switching back to other tab
        if (!sourceTabbedPane.getTitleAt(index).equals("Admin")) {
            jButton1.setEnabled(false);
            jButton6.setEnabled(false);
            jButton8.setEnabled(false);
            jLabel19.setText("Logged Out");
            jLabel19.setForeground(Color.RED);
            
            jTextField4.setText("");
            jTextField5.setText("");
            jTextField6.setText("");
            jTextField8.setText("");
            jTextField7.setText("");
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    // "Confirm" button
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        fileHandler.writeMoney(Double.parseDouble(jTextField7.getText()));
        initMoney();
        showMessageDialog(null, "Client funds set to $" + jTextField7.getText());
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jTextField7FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField7FocusGained
        jTextField7.setText("");
        jTextField7.setForeground(Color.BLACK);
    }//GEN-LAST:event_jTextField7FocusGained

    private void jTextField7FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField7FocusLost
        if (jTextField7.getText().equals("")) {
            jTextField7.setText("E.g. 99.99");
            jTextField7.setForeground(Color.LIGHT_GRAY);
        }
    }//GEN-LAST:event_jTextField7FocusLost

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(sharesbrokeringclient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(sharesbrokeringclient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(sharesbrokeringclient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(sharesbrokeringclient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new sharesbrokeringclient().setVisible(true);
            } catch (DatatypeConfigurationException_Exception | ParseExceptionException | IOException ex) {
                Logger.getLogger(sharesbrokeringclient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    // End of variables declaration//GEN-END:variables

    String selectedCurrency = "";
    boolean INITIALIZED = false;
    private final FileHandler fileHandler;

    private static java.util.List<java.lang.String> getCurrencyCodes() {
        sharebrokeringclient.GetCurrencies_Service service = new sharebrokeringclient.GetCurrencies_Service();
        sharebrokeringclient.GetCurrencies port = service.getGetCurrenciesPort();
        return port.getCurrencyCodes();
    }

    private static java.util.List<org.netbeans.xml.schema.share.Stock> getStocks(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2) throws DatatypeConfigurationException_Exception, ParseExceptionException {
        org.me.stockservice.GetStocksService_Service service = new org.me.stockservice.GetStocksService_Service();
        org.me.stockservice.GetStocksService port = service.getGetStocksServicePort();
        return port.getStocks(arg0, arg1, arg2);
    }

    private static String removeStock(java.lang.String companyName) {
        org.me.stockservice.RemoveStock_Service service = new org.me.stockservice.RemoveStock_Service();
        org.me.stockservice.RemoveStock port = service.getRemoveStockPort();
        return port.removeStock(companyName);
    }

    private static String addStock(java.lang.String arg0, java.lang.String arg1, int arg2, java.lang.String arg3, java.math.BigDecimal arg4) throws DatatypeConfigurationException_Exception {
        org.me.stockservice.AddStock_Service service = new org.me.stockservice.AddStock_Service();
        org.me.stockservice.AddStock port = service.getAddStockPort();
        return port.addStock(arg0, arg1, arg2, arg3, arg4);
    }

    private static int buyStocks(int arg0, java.lang.String arg1) {
        org.me.stockservice.BuyStocks_Service service = new org.me.stockservice.BuyStocks_Service();
        org.me.stockservice.BuyStocks port = service.getBuyStocksPort();
        return port.buyStocks(arg0, arg1);
    }

    private static int sellStocks(int arg0, java.lang.String arg1) {
        org.me.stockservice.SellStocks_Service service = new org.me.stockservice.SellStocks_Service();
        org.me.stockservice.SellStocks port = service.getSellStocksPort();
        return port.sellStocks(arg0, arg1);
    }
}
