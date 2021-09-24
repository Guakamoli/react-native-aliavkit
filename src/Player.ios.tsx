import * as _ from 'lodash';
import React from 'react';
import { requireNativeComponent, NativeModules, processColor, NativeAppEventEmitter } from 'react-native';

const { RNEditViewManager } = NativeModules;
const NativeVideoEdit = requireNativeComponent('RNMovieView');