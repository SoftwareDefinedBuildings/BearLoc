#!/usr/bin/env python
# encoding: utf-8

class AndroidSensorType(object):
  """Android Sensor Type Constants.
  Ref: http://developer.android.com/reference/android/hardware/Sensor.html
  """
  TYPE_ACCELEROMETER = 1
  TYPE_ALL = -1
  TYPE_AMBIENT_TEMPERATURE = 13
  TYPE_GRAVITY = 9
  TYPE_GYROSCOPE = 4
  TYPE_LIGHT = 5
  TYPE_LINEAR_ACCELERATION = 10
  TYPE_MAGNETIC_FIELD = 2
  TYPE_ORIENTATION = 3
  TYPE_PRESSURE = 6
  TYPE_PROXIMITY = 8
  TYPE_RELATIVE_HUMIDITY = 12
  TYPE_ROTATION_VECTOR = 11
  TYPE_TEMPERATURE = 7
  
  _all_types = [TYPE_ACCELEROMETER,
                TYPE_ALL,
                TYPE_AMBIENT_TEMPERATURE,
                TYPE_GRAVITY,
                TYPE_GYROSCOPE,
                TYPE_LIGHT,
                TYPE_LINEAR_ACCELERATION,
                TYPE_MAGNETIC_FIELD,
                TYPE_ORIENTATION,
                TYPE_PRESSURE,
                TYPE_PROXIMITY,
                TYPE_RELATIVE_HUMIDITY,
                TYPE_ROTATION_VECTOR,
                TYPE_TEMPERATURE]
  
  _single_value_types = [TYPE_AMBIENT_TEMPERATURE,
                        TYPE_LIGHT,
                        TYPE_PRESSURE,
                        TYPE_PROXIMITY,
                        TYPE_RELATIVE_HUMIDITY,
                        TYPE_TEMPERATURE]
  
  _triple_value_types = [TYPE_ACCELEROMETER,
                        TYPE_GRAVITY,
                        TYPE_GYROSCOPE,
                        TYPE_LINEAR_ACCELERATION,
                        TYPE_MAGNETIC_FIELD,
                        TYPE_ORIENTATION,
                        TYPE_TEMPERATURE]
  
  _quadruple_value_types = [TYPE_ROTATION_VECTOR]
  
  
  @staticmethod
  def value_num(type):
    """Return the number of values of sensor event.
    Return -1 if the type is not a sensor."""
    if type in AndroidSensorType._single_value_types:
      return 1
    elif type in AndroidSensorType._triple_value_types:
      return 3
    elif type in AndroidSensorType._quadruple_value_types:
      return 4
    return -1


  @staticmethod
  def contains(type):
    return (type in AndroidSensorType._all_types)


class AndroidAudioFormat:
  """Android Audio Format Constants.
  Ref: http://developer.android.com/reference/android/media/AudioFormat.html
  """
  CHANNEL_CONFIGURATION_DEFAULT = 1
  CHANNEL_CONFIGURATION_INVALID = 0
  CHANNEL_CONFIGURATION_MONO = 2
  CHANNEL_CONFIGURATION_STEREO = 3
  CHANNEL_INVALID = 0
  CHANNEL_IN_BACK = 32
  CHANNEL_IN_BACK_PROCESSED = 512
  CHANNEL_IN_DEFAULT = 1
  CHANNEL_IN_FRONT = 16
  CHANNEL_IN_FRONT_PROCESSED = 256
  CHANNEL_IN_LEFT = 4
  CHANNEL_IN_LEFT_PROCESSED = 64
  CHANNEL_IN_MONO = 16
  CHANNEL_IN_PRESSURE = 1024
  CHANNEL_IN_RIGHT = 8
  CHANNEL_IN_RIGHT_PROCESSED = 128
  CHANNEL_IN_STEREO = 12
  CHANNEL_IN_VOICE_DNLINK = 32768
  CHANNEL_IN_VOICE_UPLINK = 16384
  CHANNEL_IN_X_AXIS = 2048
  CHANNEL_IN_Y_AXIS = 4096
  CHANNEL_IN_Z_AXIS = 8192
  CHANNEL_OUT_5POINT1 = 252
  CHANNEL_OUT_7POINT1 = 1020
  CHANNEL_OUT_BACK_CENTER = 1024
  CHANNEL_OUT_BACK_LEFT = 64
  CHANNEL_OUT_BACK_RIGHT = 128
  CHANNEL_OUT_DEFAULT = 1
  CHANNEL_OUT_FRONT_CENTER = 16
  CHANNEL_OUT_FRONT_LEFT = 4
  CHANNEL_OUT_FRONT_LEFT_OF_CENTER = 256
  CHANNEL_OUT_FRONT_RIGHT = 8
  CHANNEL_OUT_FRONT_RIGHT_OF_CENTER = 512
  CHANNEL_OUT_LOW_FREQUENCY = 32
  CHANNEL_OUT_MONO = 4
  CHANNEL_OUT_QUAD = 204
  CHANNEL_OUT_STEREO = 12
  CHANNEL_OUT_SURROUND = 1052
  
  
  @staticmethod
  def channel_num(channel):
    # TODO implement full version (either in server or client side)
    if channel == AndroidAudioFormat.CHANNEL_IN_MONO:
      return 1
    else:
      return 2


class AndroidAudioEncoding:
  """Android Audio Encoding Constants.
  Ref: http://developer.android.com/reference/android/media/AudioFormat.html
  """
  ENCODING_DEFAULT = 1
  ENCODING_INVALID = 0
  ENCODING_PCM_16BIT = 2
  ENCODING_PCM_8BIT = 3
  
  
  @staticmethod
  def sample_width(encoding):
    # TODO implement full version (either in server or client side)
    if encoding == AndroidAudioEncoding.ENCODING_PCM_16BIT:
      return 2
    elif  encoding == AndroidAudioEncoding.ENCODING_PCM_8BIT:
      return 1
    else:
      return 0


class AndroidAudioSource:
  """Android Audio Source Constants.
  Ref: http://developer.android.com/reference/android/media/MediaRecorder.AudioSource.html
  """
  CAMCORDER = 5
  DEFAULT = 0
  MIC = 1
  VOICE_CALL = 4
  VOICE_COMMUNICATION = 7
  VOICE_DOWNLINK = 3
  VOICE_RECOGNITION = 6
  VOICE_UPLINK = 2