package com.demo.mygson.stream;

import javax.swing.plaf.synth.SynthTextAreaUI;

final class JsonScope {

  static final int EMPTY_ARRAY = 1;

  static final int NONEMPTY_ARRAY = 2;

  static final int EMPTY_OBJECT = 3;

  static final int DANGLING_NAME = 4;

  static final int NONEMPTY_OBJECT = 5;

  static final int EMPTY_DOCUMENT = 6;

  static final int NONEMPTY_DOCUMENT = 7;

  static final int CLOSE = 8;

}
