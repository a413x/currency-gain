import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JFrame {

    private final String fixerUrl = "http://data.fixer.io/api/";
    private final String latest = "latest";

    private final double spread = 0.5 / 100; //0.5%

    private JTextField key_field = new JTextField();
    private JFormattedTextField date_field = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
    private JFormattedTextField amount = new JFormattedTextField(new NumberFormatter());
    private JButton button = new JButton("Recalculate");
    private JLabel result = new JLabel("Result");

    private JPopupMenu popupMenu = new JPopupMenu();

    private JLabel date_field_label = new JLabel("Buy date:");
    private JLabel amount_label = new JLabel("Amount of USD:");
    private JLabel result_label = new JLabel("Gain: ");
    private JLabel key_label = new JLabel("Access_key: ");

    public static void main(String[] args){
        Main app = new Main();
        app.setVisible(true);
    }

    private Main(){
        this.setBounds(200,200,400,500);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        date_field.setValue(new Date());

        Container container = this.getContentPane();
        container.setLayout(new GridLayout(9,1));
        container.add(key_label);
        container.add(key_field);
        container.add(date_field_label);
        container.add(date_field);
        container.add(amount_label);
        container.add(amount);
        container.add(button);
        container.add(result_label);
        container.add(result);

        popupMenu.add(copyAction);
        popupMenu.add(pasteAction);
        key_field.setComponentPopupMenu(popupMenu);

        amount.setValue(0);
        button.addActionListener(new BtnEventListener());
    }

    class BtnEventListener implements ActionListener {

        boolean successLatest = false;
        boolean successHistorical = false;

        JSONObject jsonObjectLatest;
        JSONObject jsonObjectHistorical;

        @Override
        public void actionPerformed(ActionEvent event) {

            String key = key_field.getText();
            String accessKey = "?access_key=" + key;
            Date dateField = (Date) date_field.getValue();
            Date currDate = new Date();
            String date = "";
            if(dateField.before(currDate)) date = date_field.getText();
            else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                date = simpleDateFormat.format(currDate);
                date_field.setValue(currDate);
            }

            try {
                jsonObjectLatest = readJSON(fixerUrl + latest + accessKey + "&symbols=USD,RUB");
                successLatest = jsonObjectLatest.getBoolean("success");

                jsonObjectHistorical = readJSON(fixerUrl + date + accessKey + "&symbols=USD,RUB");
                successHistorical = jsonObjectHistorical.getBoolean("success");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (successHistorical && successLatest){
                double rubLatest = jsonObjectLatest.getJSONObject("rates").getDouble("RUB");
                double rubHistorical = jsonObjectHistorical.getJSONObject("rates").getDouble("RUB");

                double dolLatest = jsonObjectLatest.getJSONObject("rates").getDouble("USD");
                double dolHistorical = jsonObjectHistorical.getJSONObject("rates").getDouble("USD");

                int am = ((Number)amount.getValue()).intValue();
                if(am < 0) {am = 0; amount.setValue(0);}

                double res = (rubLatest/dolLatest)*am - (rubHistorical/dolHistorical)*am*(spread+1);

                result.setText(String.valueOf(res));
            }
            else {
                String errorMsg = "";
                if(successLatest) errorMsg = (jsonObjectHistorical.getJSONObject("error").getString("info")) + ";\n";
                else errorMsg = (jsonObjectLatest.getJSONObject("error").getString("info")) + ";\n";
                result.setText("Failed getting data : \n" + errorMsg);
            }
        }
    }

    private static String readText(Reader reader) throws IOException{
        StringBuilder stringBuilder = new StringBuilder();
        int symbol;
        while ((symbol = reader.read())!= -1){
            stringBuilder.append((char) symbol);
        }
        return stringBuilder.toString();
    }

    private static JSONObject readJSON(String url) throws IOException,JSONException {
        InputStream inputStream = new URL(url).openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,Charset.forName("UTF-8")));
            String jsonText = readText(reader);
            return new JSONObject(jsonText);
        }finally {
            inputStream.close();
        }
    }

    Action copyAction = new AbstractAction("Copy") {
        @Override
        public void actionPerformed(ActionEvent e) {
            key_field.copy();
        }
    };
    Action pasteAction = new AbstractAction("Paste") {
        @Override
        public void actionPerformed(ActionEvent ae) {
            key_field.paste();
        }
    };
}
