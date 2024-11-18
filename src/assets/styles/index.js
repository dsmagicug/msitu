
import { StyleSheet } from "react-native";

const styles = StyleSheet.create({
  container: {
    paddingTop: 5,
    marginBottom: 4,
  },
  label: {
    position: 'absolute',
    left: 5,
    fontSize: 14,
    color: '#aaa',
  },
  input: {
    height: 42,
    borderBottomWidth: 1,
    borderBottomColor: 'teal',
    fontSize: 14,
    paddingHorizontal: 5,
  },
  map: {
    ...StyleSheet.absoluteFillObject,
    flex: 1
  },
  text: {
    fontFamily: "Avenir"
  },
  textMedium: {
    fontFamily: "AvenirMedium"
  },
  buttonText: {
    fontFamily: "AvenirBold"
  },
  lottie: {
    width: 50,
    height: 50
  },
  lottie2: {
    width: 100,
    height: 100,
    zIndex:100,
  },
});
export default styles;