import React from 'react';
import { requireNativeComponent, NativeModules } from 'react-native';

const UIManager = NativeModules.UIManager;

const EditView = requireNativeComponent('RNEditView');

export default class Player extends React.Component {
  render() {
    return <EditView style={{ minWidth: 100, minHeight: 100 }} {...this.props} />;
  }
}
