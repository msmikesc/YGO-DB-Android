package com.example.ygodb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.CopyDBInCallback;
import com.example.ygodb.abs.CopyDBOutCallback;
import com.example.ygodb.databinding.ActivityMainBinding;
import com.example.ygodb.ui.analyzesets.AnalyzeCardsViewModel;
import com.example.ygodb.ui.viewcardset.ViewCardSetViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import ygodb.commonlibrary.importer.ImportPricesFromYGOPROAPI;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	private ActivityResultLauncher<Intent> copyDBInIntent = null;
	private ActivityResultLauncher<Intent> copyDBOutIntent = null;
	private AppBarConfiguration mAppBarConfiguration;
	private ActivityMainBinding binding;

	public ActivityMainBinding getBinding() {
		return binding;
	}

	private static final int STORAGE_PERMISSION_CODE = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidUtil.setViewModelOwner(this);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.appBarMain.toolbar);

		DrawerLayout drawer = binding.drawerLayout;
		NavigationView navigationView = binding.navView;
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration =
				new AppBarConfiguration.Builder(R.id.nav_viewCardsSummary, R.id.nav_viewCards, R.id.nav_viewCardSet, R.id.nav_addCards,
												R.id.nav_sellCards, R.id.nav_soldCards, R.id.nav_boxLookup,
												R.id.nav_analyzeSet).setOpenableLayout(drawer).build();

		NavHostFragment navHostFragment =
				(NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);

		if (navHostFragment == null) {
			throw new RuntimeException("navHostFragment null for " + R.id.nav_host_fragment_content_main);
		}

		NavController navController = navHostFragment.getNavController();

		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);

		checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

		CopyDBInCallback copyDBInCallback = new CopyDBInCallback(this);
		CopyDBOutCallback copyDBOutCallback = new CopyDBOutCallback(this);

		copyDBInIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), copyDBInCallback);

		copyDBOutIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), copyDBOutCallback);

		ViewCardSetViewModel viewCardSetViewModel = new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(ViewCardSetViewModel.class);
		AnalyzeCardsViewModel analyzeCardsViewModel =
				new ViewModelProvider(AndroidUtil.getViewModelOwner()).get(AnalyzeCardsViewModel.class);

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<String> setNamesArrayList = AndroidUtil.getDBInstance().getDistinctSetAndArchetypeNames();
				viewCardSetViewModel.updateSetNamesDropdownList(setNamesArrayList);
				analyzeCardsViewModel.updateSetNamesDropdownList(setNamesArrayList);
			} catch (Exception e) {
				YGOLogger.logException(e);
			}
		});

	}

	// Function to check and request permission
	public void checkPermission(String permission, int requestCode) {
		// Checking if permission is not granted
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_import) {

			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.setType("*/*");
			copyDBInIntent.launch(intent);

			return true;
		} else if (id == R.id.action_export) {

			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.setType("*/*");
			copyDBOutIntent.launch(intent);

			return true;
		} else if (id == R.id.action_price_import) {

			ImportPricesFromYGOPROAPI runner = new ImportPricesFromYGOPROAPI();

			AndroidUtil.showProgressDialog(MainActivity.this);
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					runner.run(AndroidUtil.getDBInstance(), null, false);
					AndroidUtil.hideProgressDialog();
					binding.navView.post(
							() -> Snackbar.make(binding.navView, "Price Import Finished", BaseTransientBottomBar.LENGTH_LONG).show());
				} catch (Exception e) {
					YGOLogger.error("Issue trying to import prices");
					YGOLogger.logException(e);
				}
			});

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
	}

}