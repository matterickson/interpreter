package interpreter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI {
	static String gcd_func = "def int gcd(int a, int b)\nif (a = b) then \nreturn a;\nfi;\nif(a > b)then\nreturn gcd(a-b,b);\nelse\nreturn gcd(a,b-a);\nfi;\nfed\nprint gcd(21,15);\n.";
	static String func = "double s; def double f(double x, double y)    double a;    if(x > y)then        a = x + y * y;    else        a = -1;    fi;    return a;fed s = f(2, 1)+ f(1, 2)+ f(0, 0);print(s);. ";
	static String gcd = "int a,b,r;\na = 21; b = 15;\nwhile(b<>0)do\nr = a%b;\na = b;\nb = r;\nod;\nprint(a);\n.";
	static String loop = "int x,i;\nx = 0;\ni = 1;\nwhile(i< 10) do\nx = x+i*i;\ni = i+1;\nod;\nprint(x);\n.";

	static JButton butSymbolTables, but_eval, butLoop, butFunc, butGCD, butGCD_Func;
	static JCheckBox cbFormat;
	static JTextArea outputTextArea, inputTextArea;

	public static void main(String args[]) {
		JFrame frame = new JFrame("Interpreter by Matt Erickson");
		frame.setBounds(100, 0, 800, 600);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// input area
		inputTextArea = new JTextArea(loop);
		JScrollPane scroll1 = new JScrollPane(inputTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll1.setBounds(180, 20, 560, 360);

		// output area
		outputTextArea = new JTextArea("output");
		JScrollPane scroll2 = new JScrollPane(outputTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll2.setBounds(40, 400, 700, 150);

		Font font = new Font("Courier New", Font.PLAIN, 12);
		inputTextArea.setFont(font);
		outputTextArea.setFont(font);

		// start button
		butSymbolTables = new JButton("Symbol Tables");
		butSymbolTables.setBounds(12, 20, 150, 24);
		butSymbolTables.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == butSymbolTables) {
					RecursiveDescentParser parser = new RecursiveDescentParser(inputTextArea.getText());
					outputTextArea.setText(parser.output);
					if (parser.compiled && cbFormat.isSelected())
						inputTextArea.setText(parser.getFormattedInput());
				}
			}
		});
		but_eval = new JButton("Evaluate Code");
		but_eval.setBounds(12, 50, 150, 24);
		but_eval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_eval) {
					RecursiveDescentParser parser = new RecursiveDescentParser(inputTextArea.getText());
					if (parser.compiled) {
						IntermediateRepresentation intermediate = new IntermediateRepresentation(inputTextArea.getText());
						Interpreter eval = new Interpreter(intermediate.getTree(), intermediate.globalSymTable);
						outputTextArea.setText(eval.output);
						if (parser.compiled && cbFormat.isSelected())
							inputTextArea.setText(parser.getFormattedInput());
					} else {
						outputTextArea.setText(parser.output);
					}

				}
			}
		});

		cbFormat = new JCheckBox("auto-format?", null, true);
		cbFormat.setBounds(20, 110, 160, 24);

		butLoop = new JButton("Loop Example");
		butLoop.setBounds(12, 150, 150, 24);
		butLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == butLoop) {
					inputTextArea.setText(loop);
				}
			}
		});
		butFunc = new JButton("Function Example");
		butFunc.setBounds(12, 180, 150, 24);
		butFunc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == butFunc) {
					inputTextArea.setText(func);
				}
			}
		});
		butGCD = new JButton("GCD Example");
		butGCD.setBounds(12, 210, 150, 24);
		butGCD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == butGCD) {
					inputTextArea.setText(gcd);
				}
			}
		});
		butGCD_Func = new JButton("GCD Function");
		butGCD_Func.setBounds(12, 240, 150, 24);
		butGCD_Func.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == butGCD_Func) {
					inputTextArea.setText(gcd_func);
				}
			}
		});

		panel.add(butSymbolTables);
		panel.add(but_eval);
		panel.add(cbFormat);
		panel.add(butLoop);
		panel.add(butFunc);
		panel.add(butGCD);
		panel.add(butGCD_Func);

		panel.add(scroll1);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(Box.createVerticalGlue());

		panel.add(scroll2);
		frame.setVisible(true);
	}
}