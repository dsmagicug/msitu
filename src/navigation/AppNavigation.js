import * as React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import DrawerNavigation from './DrawerNavigation';

const Stack = createNativeStackNavigator();

export default function AppStackNavigation() {

  return (
      <NavigationContainer>
          <Stack.Navigator>
              <Stack.Screen
                  name="DrawerStack"
                  component={DrawerNavigation}
                  options={{headerShown: false}}
              />
          </Stack.Navigator>
      </NavigationContainer>
  );
}
