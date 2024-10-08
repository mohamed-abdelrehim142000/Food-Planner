package com.example.mealmate.veiw.home_fragment_veiw;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mealmate.model.MealArea;
import com.example.mealmate.presenter.home_fragment_presenter.HomeFragmentPresenterImpl;
import com.example.mealmate.veiw.home_fragment_veiw.related_adapter_views.ListAllFilterAdapter;
import com.example.mealmate.veiw.home_fragment_veiw.related_adapter_views.MealCategoriesPagerAdapter;
import com.example.mealmate.veiw.home_fragment_veiw.related_adapter_views.MealIngredientesPagerAdapter;
import com.example.mealmate.veiw.home_fragment_veiw.related_adapter_views.MealOfTheDayPagerAdapter;
import com.example.mealmate.R;
import com.example.mealmate.model.MealCategory;
import com.example.mealmate.model.MealIngredient;
import com.example.mealmate.model.MealRepository.MealRepository;
import com.example.mealmate.model.database.AppDataBase;
import com.example.mealmate.model.database.local_data_source.LocalDataSourceImpl;
import com.example.mealmate.model.mealDTOs.all_meal_details.MealDTO;
import com.example.mealmate.model.network.RemoteDataSourceImpl;
import com.example.mealmate.related_animation.ZoomOutPageTransformer;
import com.example.mealmate.veiw.home_fragment_veiw.home_fragment_veiw_interface.HomeFragmentView;
import com.example.mealmate.veiw.main_activity.MainActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements HomeFragmentView {
    private ViewPager2 viewPager;
    private RecyclerView categoryRecyclerView;
    private RecyclerView ingredientRecyclerView2;
    private RecyclerView areaRecyclerView3;
    private MealCategoriesPagerAdapter mealCategoriesPagerAdapter;
    private MealIngredientesPagerAdapter mealIngredientesPagerAdapter;
    private ListAllFilterAdapter<MealCategory> filterAdapterCategory;
    private ListAllFilterAdapter<MealIngredient> filterAdapterIngredient;
    private ListAllFilterAdapter<MealArea> filterAdapterArea;
    private NavController navController;

    private MealOfTheDayPagerAdapter mealOfTheDayPagerAdapter;
    private HomeFragmentPresenterImpl presenter;
    private ArrayList<MealCategory> categoryFilterList = new ArrayList<>();
    private ArrayList<MealIngredient> ingredientFilterList = new ArrayList<>();
    private ArrayList<MealArea> areaFilterList = new ArrayList<>();
    private List<MealDTO> meals = new ArrayList<>();
    private static final String TAG = "HomeFragment";
    String mode=null;
    // Access UI elements in the custom layout
    TextView title;
    TextView message;
    Button goButton;
    Button cancelButton;
    AlertDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the string passed through the bundle
        if (getArguments() != null) {
            mode = getArguments().getString("user_type");
            Log.i("user_type2", "onCreateView: "+mode);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        viewPager = view.findViewById(R.id.all_Meal_detil_ViewPager);
        categoryRecyclerView = view.findViewById(R.id.viewPagerCategory);
        ingredientRecyclerView2 =view.findViewById(R.id.viewPagerIngredient);
        areaRecyclerView3=view.findViewById(R.id.viewPagerArea);
        navController = Navigation.findNavController(requireView());
        meals=new ArrayList<>();


        // Create an instance of AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
        // Set the custom layout to the dialog
        builder.setView(dialogView);
        // Create and show the dialog
        dialog = builder.create();
        title = dialogView.findViewById(R.id.custom_title);
        message = dialogView.findViewById(R.id.custom_message);
        goButton = dialogView.findViewById(R.id.button_go);
        cancelButton = dialogView.findViewById(R.id.button_cancel);

        presenter = new HomeFragmentPresenterImpl(
                 MealRepository.getInstance(
                LocalDataSourceImpl.getInstance(
                        AppDataBase.getInstance(getContext()).getFavoriteMealDAO(),
                        AppDataBase.getInstance(getContext()).getMealPlanDAO()
                ),
                RemoteDataSourceImpl.getInstance()),

                this);



        // Setup filter RecyclerViews
        filterAdapterCategory = createFilterAdapter(categoryFilterList, MealCategory.class);
        filterAdapterIngredient = createFilterAdapter(ingredientFilterList, MealIngredient.class);
        filterAdapterArea=createFilterAdapter(areaFilterList,MealArea.class);

        setupRecyclerView(categoryRecyclerView, filterAdapterCategory);
        setupRecyclerView(ingredientRecyclerView2, filterAdapterIngredient);
        setupRecyclerView(areaRecyclerView3, filterAdapterArea);

        // Load data
        presenter.loadMeals();
        presenter.loadMealsCategory();
        presenter.loadMealsIngredient();
        presenter.loadMealsArea();

        mealOfTheDayPagerAdapter = new MealOfTheDayPagerAdapter(getContext(), meals, meal -> {

           if ("guest".equals(mode)) {
               showRestrictedAccessDialog();
           }else{
               HomeFragmentDirections.ActionHomeFragmentToAllMealDetailsFragment action =
                       HomeFragmentDirections.actionHomeFragmentToAllMealDetailsFragment(meal.getIdMeal(),"homeFragment");
               navController.navigate(action);
               Toast.makeText(getContext(), meal.getStrMeal(), Toast.LENGTH_SHORT).show();
           }
        });
        viewPager.setAdapter(mealOfTheDayPagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
    }


    private <T> ListAllFilterAdapter<T> createFilterAdapter(List<T> filterList, Class<T> type) {
        return new ListAllFilterAdapter<>(
                getContext(),
                filterList,
                meal -> {
                    if (type.equals(MealCategory.class)) {
                        HomeFragmentDirections.ActionHomeFragmentToSearchFragment action = HomeFragmentDirections.actionHomeFragmentToSearchFragment();
                        action.setFilterName(((MealCategory) meal).getStrCategory());
                        action.setFilterType("category");
                        if (mode!=null && mode.equals("guest")){
                            action.setUserType(mode);
                        }
                        navController.navigate(action);
                        Log.i(TAG, "createFilterAdapter: "+ ((MealCategory) meal).getStrCategory());
                        Toast.makeText(getContext(), ((MealCategory) meal).getStrCategory(), Toast.LENGTH_SHORT).show();
                    } else if (type.equals(MealIngredient.class)) {
                        HomeFragmentDirections.ActionHomeFragmentToSearchFragment action = HomeFragmentDirections.actionHomeFragmentToSearchFragment();
                        action.setFilterName(((MealIngredient) meal).getStrIngredient());
                        action.setFilterType("ingredient");
                        if (mode!=null && mode.equals("guest")){
                            action.setUserType(mode);
                        }
                        navController.navigate(action);
                        Toast.makeText(getContext(), ((MealIngredient) meal).getStrIngredient(), Toast.LENGTH_SHORT).show();
                    }else if (type.equals(MealArea.class)) {
                        HomeFragmentDirections.ActionHomeFragmentToSearchFragment action = HomeFragmentDirections.actionHomeFragmentToSearchFragment();
                        action.setFilterName(((MealArea) meal).getStrArea());
                        action.setFilterType("area");
                        if (mode!=null && mode.equals("guest")){
                            action.setUserType(mode);
                        }
                        navController.navigate(action);
                        Toast.makeText(getContext(), ((MealArea) meal).getStrArea(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        Log.i(TAG, "setupRecyclerView: "+recyclerView.getAdapter());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showData(List data) {
        if (data == null || data.isEmpty()) return;

        if (data.get(0) instanceof MealDTO) {
            meals.clear();
            meals.addAll((List<MealDTO>) data);
            mealOfTheDayPagerAdapter.notifyDataSetChanged();

        } else if (data.get(0) instanceof MealCategory) {
            Log.i(TAG, "showData: "+((List<MealCategory>) data).size());
            filterAdapterCategory.updateFilterList((List<MealCategory>) data);
        }else if (data.get(0) instanceof MealIngredient) {
            filterAdapterIngredient.updateFilterList((List<MealIngredient>) data);
        }else if (data.get(0) instanceof MealArea) {
            filterAdapterArea.updateFilterList((List<MealArea>) data);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    // Method to display the restricted access popup
    private void showRestrictedAccessDialog() {

        title.setText(R.string.sign_up_for_more_features);
        message.setText(R.string.add_your_food_preferences_plan_your_meals_and_more);
        goButton.setText(R.string.sign_up);
        cancelButton.setText(R.string.cancel);

        goButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("destination_fragment", "startFragment");

            // Start the MainActivity
            if (getContext() != null) {
                getContext().startActivity(intent);

                // Finish the current Activity if this method is called within an Activity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }


}
