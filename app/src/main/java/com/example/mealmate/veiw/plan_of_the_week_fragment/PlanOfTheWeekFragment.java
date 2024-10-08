
package com.example.mealmate.veiw.plan_of_the_week_fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mealmate.receiver.NetworkChangeReceiver;
import com.example.mealmate.utils.NetworkUtils;
import com.example.mealmate.veiw.plan_of_the_week_fragment.plan_of_the_week_fragment_interface.Handel_Delete_Plans;
import com.example.mealmate.veiw.plan_of_the_week_fragment.plan_of_the_week_fragment_interface.PlanHandelSeeMoreClick;
import com.example.mealmate.veiw.plan_of_the_week_fragment.related_adpter.PlanMealPagerAdapter;
import com.example.mealmate.presenter.plan_of_the_week_fragment_presenter.PlanOfTheWeekFragmentPresenter;
import com.example.mealmate.veiw.plan_of_the_week_fragment.plan_of_the_week_fragment_interface.PlanOfWeekFragmentVeiwInterface;
import com.example.mealmate.R;
import com.example.mealmate.model.MealRepository.MealRepository;
import com.example.mealmate.model.database.AppDataBase;
import com.example.mealmate.model.database.local_data_source.LocalDataSourceImpl;
import com.example.mealmate.model.mealDTOs.all_meal_details.MealDTO;
import com.example.mealmate.model.mealDTOs.meal_plan.MealPlan;
import com.example.mealmate.model.network.RemoteDataSourceImpl;
import com.example.mealmate.related_animation.ZoomOutPageTransformer;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PlanOfTheWeekFragment extends Fragment implements PlanOfWeekFragmentVeiwInterface, PlanHandelSeeMoreClick, Handel_Delete_Plans {

    private ViewPager2 viewPager;
    private PlanMealPagerAdapter planMealPagerAdapter;
    private PlanOfTheWeekFragmentPresenter presenter;
    private ArrayList<MealDTO> mealDTOS = new ArrayList<>();
    private ArrayList<MealDTO> filterMealDTOs = new ArrayList<>();
    private ArrayList<MealPlan> mealPlans = new ArrayList<>();
    private ArrayList<MealPlan> filterMealPlans = new ArrayList<>();
    private SimpleDateFormat dateFormat;
    private int currentWeekIndex;
    private TextView weekRangeTextView;

    private ImageButton prevWeekButton;
    private ImageButton nextWeekButton;

    private List<String> lsitAvailableDays;
    private List<String> weekRanges;


    private static final String TAG = "PlanOfTheWeekFragment";
    private static final String TAG2 = "PlanOfTheWeekFragment2";

    private NavController navController;
    private ProgressBar progressBar;

    private Button butAll, butMonday, butTuesday, butWednesday, butThursday, butFriday, butSaturday, butSunday;
    // Access UI elements in the custom layout
    TextView title;
    TextView message;
    Button goButton;
    Button cancelButton;
    AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plan_of_the_week, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        viewPager = view.findViewById(R.id.favorite_Meal_ViewPager);
        progressBar = view.findViewById(R.id.progressBar);
        navController = Navigation.findNavController(view);
        butAll = view.findViewById(R.id.butAll);
        butMonday = view.findViewById(R.id.btnMonday);
        butTuesday = view.findViewById(R.id.btnTuesday);
        butWednesday = view.findViewById(R.id.btnWednesday);
        butThursday = view.findViewById(R.id.btnThursday);
        butFriday = view.findViewById(R.id.btnFriday);
        butSaturday = view.findViewById(R.id.btnSaturday);
        butSunday = view.findViewById(R.id.btnSunday);
        prevWeekButton = view.findViewById(R.id.prevWeekButton);
        nextWeekButton = view.findViewById(R.id.nextWeekButton);
        weekRangeTextView = view.findViewById(R.id.weekRangeTextView);


        presenter = new PlanOfTheWeekFragmentPresenter(
                MealRepository.getInstance(
                        LocalDataSourceImpl.getInstance(
                                AppDataBase.getInstance(getContext()).getFavoriteMealDAO(),
                                AppDataBase.getInstance(getContext()).getMealPlanDAO()
                        ),
                        RemoteDataSourceImpl.getInstance()),

                this
        );

        mealDTOS = new ArrayList<>();
        mealPlans = new ArrayList<>();
        filterMealDTOs = new ArrayList<>();
        filterMealPlans = new ArrayList<>();
        weekRanges = new ArrayList<>();
        lsitAvailableDays = new ArrayList<>();


        // Initialize the adapter
        planMealPagerAdapter = new PlanMealPagerAdapter(getContext(), filterMealDTOs, filterMealPlans, this, this);
        viewPager.setAdapter(planMealPagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        presenter.getAllPlanOfWeeksMeals(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        presenter.getWeekRange(mealPlans);
        presenter.getDays(mealPlans);

        // Set onClick listeners

        butAll.setOnClickListener(v -> filterList("All", butAll));
        butMonday.setOnClickListener(v -> filterList("Monday", butMonday));
        butTuesday.setOnClickListener(v -> filterList("Tuesday", butTuesday));
        butWednesday.setOnClickListener(v -> filterList("Wednesday", butWednesday));
        butThursday.setOnClickListener(v -> filterList("Thursday", butThursday));
        butFriday.setOnClickListener(v -> filterList("Friday", butFriday));
        butSaturday.setOnClickListener(v -> filterList("Saturday", butSaturday));
        butSunday.setOnClickListener(v -> filterList("Sunday", butSunday));


        dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        // Initialize the current week index to the first week in the list
        currentWeekIndex = 0;

        // Display the current week range
        prevWeekButton.setOnClickListener(v -> {
            // Move to the previous week if possible
            if (currentWeekIndex > 0) {
                currentWeekIndex--;
                presenter.updateWeekRange(currentWeekIndex, weekRanges, dateFormat);
                filterList(weekRanges.get(currentWeekIndex), null);
                resetButtonBackgrounds();
                presenter.getDays(filterMealPlans);
                updateDayButtons();
            }
        });

        nextWeekButton.setOnClickListener(v -> {
            // Move to the next week if possible
            if (currentWeekIndex < weekRanges.size() - 1) {
                currentWeekIndex++;
                presenter.updateWeekRange(currentWeekIndex, weekRanges, dateFormat);
                filterList(weekRanges.get(currentWeekIndex), null);
                resetButtonBackgrounds();
                presenter.getDays(filterMealPlans);
                updateDayButtons();
            }
        });
    }


    public void setAvailableDays(List<String> days) {
        lsitAvailableDays.clear();
        lsitAvailableDays.addAll(days);
        Log.i(TAG, "setAvailableDays: " + lsitAvailableDays.toString());
    }

    @Override
    public void updateWeekRangeText(String weekRange) {
        weekRangeTextView.setText(weekRange);

    }

    @Override
    public void setWeekRange(List<String> weekRanges) {
        this.weekRanges.clear();
        this.weekRanges.addAll(weekRanges);
        Log.i(TAG2, "setWeekRange: " + weekRanges.toString());

    }

    @Override
    public void setDay(List<String> days) {
        this.lsitAvailableDays.clear();
        this.lsitAvailableDays.addAll(days);
    }

    public void updateDayButtons() {
        Log.i(TAG, "updateDayButtons: " + lsitAvailableDays.toString());

        // Create a map to associate day names with their corresponding buttons
        Map<String, Button> dayButtonMap = new HashMap<>();
        dayButtonMap.put("Monday", butMonday);
        dayButtonMap.put("Tuesday", butTuesday);
        dayButtonMap.put("Wednesday", butWednesday);
        dayButtonMap.put("Thursday", butThursday);
        dayButtonMap.put("Friday", butFriday);
        dayButtonMap.put("Saturday", butSaturday);
        dayButtonMap.put("Sunday", butSunday);

        // Disable all buttons initially
        for (Button button : dayButtonMap.values()) {
            button.setEnabled(false);
        }

        // Enable buttons for days present in the availableDays list
        for (String day : lsitAvailableDays) {
            Button button = dayButtonMap.get(day);
            if (button != null) {
                button.setEnabled(true);
            }
        }
    }

    private void resetButtonBackgrounds() {
        butMonday.setBackgroundResource(R.drawable.day_button_background);
        butTuesday.setBackgroundResource(R.drawable.day_button_background);
        butWednesday.setBackgroundResource(R.drawable.day_button_background);
        butThursday.setBackgroundResource(R.drawable.day_button_background);
        butFriday.setBackgroundResource(R.drawable.day_button_background);
        butSaturday.setBackgroundResource(R.drawable.day_button_background);
        butSunday.setBackgroundResource(R.drawable.day_button_background);
        butAll.setBackgroundResource(R.drawable.day_button_background);
    }


    public void filterList(@NonNull String dayName, Button button) {
        if (button != null) {
            resetButtonBackgrounds();
            button.setBackgroundResource(R.drawable.day_button_backgroundv2);
        }
        Log.i(TAG, "filterList: " + dayName);

        filterMealPlans.clear();
        filterMealDTOs.clear();
        lsitAvailableDays.clear();

        Set<String> filteredMealIds = new HashSet<>();

        if ("All".equals(dayName) || "Select Week".equals(dayName)) {
            filterMealPlans.addAll(mealPlans);
            filterMealDTOs.addAll(mealDTOS);
            weekRangeTextView.setText("Select Week");
            presenter.getDays(filterMealPlans);
            updateDayButtons();
        } else {
            for (MealPlan mealPlan : mealPlans) {
                boolean shouldAdd = dayName.contains("-")
                        ? dayName.equals(mealPlan.getDate())
                        : dayName.equals(mealPlan.getDayOfWeek());

                if (shouldAdd) {
                    filterMealPlans.add(mealPlan);
                    filteredMealIds.add(mealPlan.getMealId());
                }
            }

            for (MealDTO mealDTO : mealDTOS) {
                if (filteredMealIds.contains(mealDTO.getIdMeal())) {
                    filterMealDTOs.add(mealDTO);
                }
            }
        }
        Log.i(TAG, "filterList:=== " + lsitAvailableDays.toString());
        planMealPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showData(List<MealDTO> data, List<MealPlan> planMeals) {
        // Initialize the ProgressBar

        mealDTOS.clear();
        mealPlans.clear();
        weekRanges.clear();
        filterMealDTOs.clear();
        filterMealPlans.clear();
        lsitAvailableDays.clear();
        weekRanges.clear();
        filterMealDTOs.addAll(data);
        filterMealPlans.addAll(planMeals);
        mealPlans.addAll(planMeals);
        mealDTOS.addAll(data);
        presenter.getWeekRange(mealPlans);
        presenter.updateWeekRange(currentWeekIndex, weekRanges, dateFormat);
        presenter.getDays(mealPlans);
        updateDayButtons();
        planMealPagerAdapter.notifyDataSetChanged();
        Log.i(TAG, "showData: " + mealDTOS.size() + mealDTOS.get(0).getStrMeal() + mealDTOS.get(0).getIdMeal()
                + mealDTOS.get(0).getStrMealThumb() + mealDTOS.get(0).getStrArea() + mealDTOS.get(0).getStrCategory());
        Log.d(TAG, "showData: " + mealDTOS.size());
    }

    @Override
    public void onSeeMoreClick(MealDTO meal, MealPlan mealPlan) {
        String id = meal.getIdMeal();
        if (id != null && mealPlan != null) {
            PlanOfTheWeekFragmentDirections.ActionPlanOfTheWeekFragmentToAllMealDetailsFragment action =
                    PlanOfTheWeekFragmentDirections.actionPlanOfTheWeekFragmentToAllMealDetailsFragment(id, "planOfTheWeekFragment");
            action.setMealPlan(mealPlan);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(action);

        }


    }

    @Override
    public void onDeletePlansClick(MealPlan mealPlan) {

        title.setText(R.string.wait_are_you_sure);
        message.setText(R.string.delete_your_meals_from_plans);
        goButton.setText(R.string.yes);
        cancelButton.setText(R.string.cancel);

        goButton.setOnClickListener(v -> {
            presenter.deletePlanMeal(mealPlan);
            dialog.dismiss();
            refreshFragment();

        });
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();


    }


    @Override
    public void showError() {
        Log.i(TAG2, "showError: "+NetworkUtils.isNetworkAvailable(getContext()));
            title.setText(R.string.no_data_found);
            message.setText(R.string.add_your_meals_to_plan_first);
            goButton.setText(R.string.ok);
            cancelButton.setText(R.string.back_to_home);
            goButton.setOnClickListener(v -> {
                if (NetworkUtils.isNetworkAvailable(getContext())) {
                    navController.navigate(R.id.action_planOfTheWeekFragment_to_searchFragment);
                    dialog.dismiss();
                }else{
                    dialog.dismiss();
                    title.setText(R.string.open_internet_connection_for_more_features);
                    message.setText(R.string.add_your_food_preferences_plan_your_meals_and_more);
                    goButton.setText(R.string.ok);
                    cancelButton.setVisibility(View.GONE);

                    goButton.setOnClickListener(vs -> {
                        dialog.dismiss();
                    });
                    dialog.show();
                }

            });
            cancelButton.setOnClickListener(v -> {
                if (NetworkUtils.isNetworkAvailable(getContext())) {
                    navController.navigate(R.id.action_planOfTheWeekFragment_to_homeFragment);
                    dialog.dismiss();
                }else{
                    dialog.dismiss();
                    title.setText(R.string.open_internet_connection_for_more_features);
                    message.setText(R.string.add_your_food_preferences_plan_your_meals_and_more);
                    goButton.setText(R.string.ok);
                    cancelButton.setVisibility(View.GONE);

                    goButton.setOnClickListener(vs -> {
                        dialog.dismiss();
                    });
                    dialog.show();
                }
            });
            dialog.show();
    }


    public void refreshFragment() {
        navController.navigate(R.id.planOfTheWeekFragment);

    }


}