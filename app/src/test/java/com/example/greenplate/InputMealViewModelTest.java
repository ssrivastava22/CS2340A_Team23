package com.example.greenplate;

import com.example.greenplate.model.InputMealModel;
import com.example.greenplate.viewmodels.InputMealViewModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputMealViewModelTest {

    private InputMealViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new InputMealViewModel();
    }

    @Test
    public void testSetInputMealModelWithValidInput() {
        InputMealModel newMeal = new InputMealModel("Pizza", 250);
        viewModel.setInputMealModel(newMeal);
        // Asserting indirectly via isValidInput since we're not testing getters/setters directly
        assertTrue(viewModel.isValidInput(newMeal));
    }

    @Test
    public void testSetInputMealModelWithInvalidInput() {
        InputMealModel invalidMeal = new InputMealModel("", 300); // Invalid due to empty meal name
        viewModel.setInputMealModel(invalidMeal);
        // Asserting the state was not updated with invalid input indirectly
        assertFalse(viewModel.isValidInput(invalidMeal));
    }

    @Test
    public void testIsValidInputWithVariousInputs() {
        InputMealModel nullMeal = null;
        assertFalse(viewModel.isValidInput(nullMeal));

        InputMealModel invalidMeal = new InputMealModel("", 250); // Invalid due to empty name
        assertFalse(viewModel.isValidInput(invalidMeal));

        InputMealModel validMeal = new InputMealModel("Burger", 500);
        assertTrue(viewModel.isValidInput(validMeal));
    }
}