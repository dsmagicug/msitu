
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
  }
});
export default styles;