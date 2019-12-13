package app.controller;

import app.StudentCalc;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pkgLogic.Payment;
import pkgLogic.Loan;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import org.apache.poi.ss.formula.functions.FinanceLib;

import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.DatePicker;

public class LoanCalcViewController implements Initializable   {

	private StudentCalc stuc = null;
	
	@FXML
	private TextField LoanAmount;
	@FXML
	private TextField InterestRate;
	@FXML
	private TextField NbrOfYears;
	@FXML
	private DatePicker PaymentStartDate;
	@FXML
	private TextField AdditionalPayment;
	
	
	@FXML
	private Label lblTotalPayemnts;
	@FXML
	private Label lblTotalInterest;
	@FXML
	private Label lblScheduledPayments;
	@FXML
	private Label lblActualNumberOfPayments;
	@FXML
	private Label lblTotalEarlyPayments;

	
	@FXML
	private TableView<Payment> tvResults;
	
	@FXML
	private TableColumn<Payment, Integer> colPaymentNumber;
	@FXML
	private TableColumn<Payment, LocalDate> colDueDate;
	@FXML
	private TableColumn<Payment, Double> colPayment;
	@FXML
	private TableColumn<Payment, Double> colAdditionalPayment;
	@FXML
	private TableColumn<Payment, Double> colInterest;
	@FXML
	private TableColumn<Payment, Double> colPrinciple;
	@FXML
	private TableColumn<Payment, Double> colBalance;
	
	
	private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
	
	//TODO: Account for all the other columns		
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colPaymentNumber.setCellValueFactory(new PropertyValueFactory<>("paymentNbr"));
		colDueDate.setCellValueFactory(new PropertyValueFactory<>("DueDate"));
		colPayment.setCellValueFactory(new PropertyValueFactory<>("Payment"));
		colAdditionalPayment.setCellValueFactory(new PropertyValueFactory<>("AdditionalPayment"));
		colInterest.setCellValueFactory(new PropertyValueFactory<>("Interest"));
		colPrinciple.setCellValueFactory(new PropertyValueFactory<>("Principle"));
		colBalance.setCellValueFactory(new PropertyValueFactory<>("Balance"));
		
		tvResults.setItems(paymentList);
	}

	public void setMainApp(StudentCalc sc) {
		this.stuc = sc;
	}
	
	
	public double decimal2(double d) {
		return (double)(Math.round(d * 100) / 100.0);
	}
	/**
	 * btnCalcLoan - Fire this event when the button clicks
	 * 
	 * @version 1.0
	 * @param event
	 */
	@FXML
	private void btnCalcLoan(ActionEvent event) {
		
		double dLoanAmount = decimal2(Double.parseDouble(LoanAmount.getText()));
		LocalDate localDate = PaymentStartDate.getValue();
		double dInterestRate = Double.parseDouble(InterestRate.getText());
		double dAdditionalPayment = decimal2(Double.parseDouble(AdditionalPayment.getText()));
		int iNbrOfYears = Integer.parseInt(NbrOfYears.getText());
		
		double pmt = decimal2(Math.abs(FinanceLib.pmt(dInterestRate, iNbrOfYears * 12, dLoanAmount, 0, false)));
		System.out.println(pmt);
		Loan loan = new Loan(dLoanAmount, dInterestRate, pmt, iNbrOfYears, localDate, dAdditionalPayment);
		
		
		
		/*
		 * When this button is clicked, you need to do the following:
		 * 
		 * 1) Clear the table
		 * 2) Clear the results fields (Total Payments, Total Interest)
		 * 3) You're going to create 'n' payments based on the data you give.  You'll calculate and
		 * 		carry forward 'balance', because you're going to have to hand calculate that month's
		 * 		interest.
		 * Payment# - you'll set this, counting from 1 to N
		 * Due Date - based on the given date.  method .plusMonths(1) will calculate date + 1 month.
		 * Payment  - Calculate based on PMT function (which is your minimum payment)
		 * Additional Payment - based on Additional Payment given by user
		 * Interest - Calculate based on 
		 */
		
		// 1) Clear the table
		tvResults.getItems().clear();

		// 2) Clear the results fields (Total Payments, Total Interest)
		
		
		// 3) create payments
		int paymentNbr = 1;
		double interest = 0;
		double principle = 0;
		double balance = loan.getLoanAmount();
		double totalPayment = 0;
		double totalInterest = 0;
		double totalEarly = 0;
		while(balance > 0){
			if(balance < loan.getPMT()){
				loan.setPMT(balance);
			}
		    interest = decimal2(balance * (dInterestRate / 12));
		    principle = decimal2(loan.getPMT() + dAdditionalPayment - interest);
			balance = decimal2(balance - principle);
			if(balance < 0) {
				balance = 0;
				dAdditionalPayment = 0;
			}
			paymentList.add(new Payment(paymentNbr++, localDate, loan.getPMT(), dAdditionalPayment, interest, principle, balance));
			localDate = localDate.plusMonths(1);
			totalPayment = totalPayment + principle;
			totalInterest = totalInterest + interest;
			totalEarly = totalEarly + dAdditionalPayment;
		}
		tvResults.setItems(paymentList);
		
		lblScheduledPayments.setText(Double.toString(pmt));
		lblActualNumberOfPayments.setText(Integer.toString(paymentNbr - 1));
		lblTotalEarlyPayments.setText(Double.toString(totalEarly));
		lblTotalPayemnts.setText(Double.toString(decimal2(totalPayment)));
		lblTotalInterest.setText(Double.toString(decimal2(totalInterest)));
	}
	
}
