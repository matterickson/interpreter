package interpreter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MyApplet extends JApplet {
	private static final long serialVersionUID = 7831381194288802123L;
	String s4 = "def int gcd(int a, int b)\nif (a = b) then \nreturn a;\nfi;\nif(a > b)then\nreturn gcd(a-b,b);\nelse\nreturn gcd(a,b-a);\nfi;\nfed\nprint gcd(21,15);\n.";
	String s3 = "double s; def double f(double x, double y)    double a;    if(x > y)then        a = x + y * y;    else        a = -1;    fi;    return a;fed s = f(2, 1)+ f(1, 2)+ f(0, 0);print(s);. ";
	String s2 = "int a,b,r;\na = 21; b = 15;\nwhile(b<>0)do\nr = a%b;\na = b;\nb = r;\nod;\nprint(a);\n.";
	String s1 = "int x,i;\nx = 0;\ni = 1;\nwhile(i< 10) do\nx = x+i*i;\ni = i+1;\nod;\nprint(x);\n.";

	JButton jbut, but_a3, but_eval, but_s1, but_s2, but_s3, but_s4, but_s5;
	JCheckBox j;
	JTextArea outputTextArea, inputTextArea;
	
	@Override
	public void init(){
		this.setSize(600, 600);
		JPanel panel = new JPanel();
		this.getContentPane().add(panel);
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// input area
		inputTextArea = new JTextArea(s1);
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
		jbut = new JButton("Symbol Tables");
		jbut.setBounds(12, 20, 150, 24);
		jbut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == jbut) {
					RecursiveDescentParser parser = new RecursiveDescentParser(inputTextArea.getText());
					outputTextArea.setText(parser.output);
					if (parser.compiled && j.isSelected())
						inputTextArea.setText(parser.getFormattedInput());
				}
			}
		});
		but_a3 = new JButton("A3 Intermediate Representation");
		but_a3.setBounds(12, 50, 150, 24);
		but_a3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_a3) {
					RecursiveDescentParser parser = new RecursiveDescentParser(inputTextArea.getText());
					if (parser.compiled) {
						IntermediateRepresentation intermediate = new IntermediateRepresentation(
								inputTextArea.getText());
						outputTextArea.setText(intermediate.output);
						if (parser.compiled && j.isSelected())
							inputTextArea.setText(parser.getFormattedInput());
					} else {
						outputTextArea.setText(parser.output);
					}
				}
			}
		});
		but_eval = new JButton("Evaluate Code");
		but_eval.setBounds(12, 80, 150, 24);
		but_eval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_eval) {
					RecursiveDescentParser parser = new RecursiveDescentParser(inputTextArea.getText());
					if (parser.compiled) {
						IntermediateRepresentation intermediate = new IntermediateRepresentation(inputTextArea.getText());
						Interpreter eval = new Interpreter(intermediate.getTree(), intermediate.globalSymTable);
						outputTextArea.setText(eval.output);
						if (parser.compiled && j.isSelected())
							inputTextArea.setText(parser.getFormattedInput());
					} else {
						outputTextArea.setText(parser.output);
					}

				}
			}
		});

		j = new JCheckBox("auto-format?");
		j.setEnabled(true);
		j.setBounds(20, 110, 160, 24);

		but_s1 = new JButton("test 1");
		but_s1.setBounds(12, 150, 150, 24);
		but_s1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_s1) {
					inputTextArea.setText(s1);
				}
			}
		});
		but_s2 = new JButton("test 2");
		but_s2.setBounds(12, 180, 150, 24);
		but_s2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_s2) {
					inputTextArea.setText(s2);
				}
			}
		});
		but_s3 = new JButton("test 3");
		but_s3.setBounds(12, 210, 150, 24);
		but_s3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_s3) {
					inputTextArea.setText(s3);
				}
			}
		});
		but_s4 = new JButton("test 4");
		but_s4.setBounds(12, 240, 150, 24);
		but_s4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == but_s4) {
					inputTextArea.setText(s4);
				}
			}
		});

		panel.add(jbut);
		panel.add(but_a3);
		panel.add(but_eval);
		panel.add(j);
		panel.add(but_s1);
		panel.add(but_s2);
		panel.add(but_s3);
		panel.add(but_s4);

		panel.add(scroll1);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(Box.createVerticalGlue());

		panel.add(scroll2);
	}
}
