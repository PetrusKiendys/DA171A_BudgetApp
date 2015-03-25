package se.kiendys.petrus.da171a.uppg3.budgetapp.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import se.kiendys.petrus.da171a.uppg3.budgetapp.BudgetConstants;
import se.kiendys.petrus.da171a.uppg3.budgetapp.DBAdapter;
import se.kiendys.petrus.da171a.uppg3.budgetapp.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ExpensesActivity extends Activity {
	
	// database
	private DBAdapter db;
	
	// views
	@SuppressWarnings("unused")
	private Button btnDate, btnSave;
	private Spinner spCategory;
	private EditText etExpenses;
	
	// other
	private String[] categoryStrArray;
	private int year, month, day;
	private String txtDate;
	
	private static final int ID_DATE_DIALOG = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_activity_expenses);

		db = new DBAdapter(this);

		btnDate = (Button) findViewById(R.id.btnExpensesActivityDate);
		btnSave = (Button) findViewById(R.id.btnExpensesActivitySave);
		etExpenses = (EditText) findViewById(R.id.etExpensesActivityExpenses);
		spCategory = (Spinner) findViewById(R.id.spExpensesActivityCategory);

		initializeViews();
	}
	
	private void initializeViews() {
		Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - initializeViews();\nCalendar - " +
				"year: " + year + ", " +
				"month: " + month + ", " +
				"day: " + day);
				
		SimpleDateFormat sdf = new SimpleDateFormat("E dd-MMM-yyyy");
		txtDate = sdf.format(cal.getTime());
		btnDate.setText(txtDate);
		
		categoryStrArray = getResources().getStringArray(R.array.gui_activity_expenses_category_array);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryStrArray);
		spCategory.setAdapter(adapter);
	}

	protected Dialog onCreateDialog(int id) {
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onCreateDialog(int); id: "+id);
		switch (id) {
		case ID_DATE_DIALOG:
			return new DatePickerDialog(this, dateListener, year, month, day);
		}
		return null;
	}

	public void onClickDate(View v) {
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickDate();");
		showDialog(ID_DATE_DIALOG);
	}

	private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker dp, int inYear, int inMonth, int inDay) {
			Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - OnDateSetListener.onDateSet(...);");
			Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onDateSet(...); inYear: "+inYear+", year: "+year);
			
			year = inYear;
			month = inMonth;
			day = inDay;

			Date date = new Date(inYear-1900, inMonth, inDay);	// negative offset by a value of 1900 due to the way Date objects are constructed with the provided arguments
			Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onDateSet(...); Date: "+date.toString());
			SimpleDateFormat sdf = new SimpleDateFormat("E dd-MMM-yyyy");
			txtDate = sdf.format(date);
			btnDate.setText(txtDate);
			Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onDateSet(...); txtDate: "+txtDate);
		}
	};

	public void onClickSave(View v) {
		String dbType = getResources().getString(R.string.db_type_expense);
		
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); year month day: "+year+" "+month+" "+day);
		String dbDate = year+"-"+(month+1)+"-"+day;
		
		String dbCategory = categoryStrArray[spCategory.getSelectedItemPosition()];
		
		String dbAmount = etExpenses.getText().toString();
		if (dbAmount.isEmpty()) {
			showErrorMessage(BudgetConstants.ERROR_INVALID_AMOUNT_EXPENSES);
			return;
		}
		
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); dbType: "+dbType);
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); dbDate: "+dbDate);
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); dbCategory: "+dbCategory);
		Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); dbAmount: "+dbAmount);
		
		db.open();
		long rowID = db.insertEntry(dbType, dbDate, dbCategory, dbAmount);
		
		
		Cursor c = db.getEntry(rowID);
		if(c != null) {
			if (c.moveToFirst()) {
				Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); The entry was succesfully added to the database");
				Toast.makeText(this, R.string.gui_activity_expenses_success_adding_entry, Toast.LENGTH_SHORT).show();
			} else {
				Log.d(BudgetConstants.DEBUG_TAG, "ExpensesActivity - onClickSave(View); The entry failed to be added to the database");
				Toast.makeText(this, R.string.gui_activity_expenses_failed_adding_entry, Toast.LENGTH_SHORT).show();
			}
		}
		db.close();
		finish();
			
	}

	private void showErrorMessage(String errorMsg) {
		// REMARK: this method could be further improved to aesthetically look like an error message
		Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
	}

}
