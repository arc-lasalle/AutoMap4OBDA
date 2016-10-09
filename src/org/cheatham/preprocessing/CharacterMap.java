package org.cheatham.preprocessing;

import java.util.HashMap;

public class CharacterMap {

	private static HashMap<Integer, String> charMap = 
		new HashMap<Integer, String>();

	private static void init() {

		// Chinese
		// TODO
		
		// Czech
		charMap.put(193, "A");
		charMap.put(225, "a");
		charMap.put(260, "A");
		charMap.put(261, "a");
		charMap.put(196, "A");
		charMap.put(228, "a");
		charMap.put(201, "E");
		charMap.put(233, "e");
		charMap.put(280, "E");
		charMap.put(281, "e");
		charMap.put(282, "E");
		charMap.put(283, "e");
		charMap.put(205, "I");
		charMap.put(237, "i");
		charMap.put(211, "O");
		charMap.put(243, "o");
		charMap.put(212, "O");
		charMap.put(244, "o");
		charMap.put(218, "U");
		charMap.put(250, "u");
		charMap.put(366, "U");
		charMap.put(367, "u");
		charMap.put(221, "Y");
		charMap.put(253, "y");
		charMap.put(268, "C");
		charMap.put(269, "c");
		charMap.put(271, "d");
		charMap.put(357, "t");
		charMap.put(313, "L");
		charMap.put(314, "l");
		charMap.put(327, "N");
		charMap.put(328, "n");
		charMap.put(340, "R");
		charMap.put(341, "r");
		charMap.put(344, "R");
		charMap.put(345, "r");
		charMap.put(352, "S");
		charMap.put(353, "s");
		charMap.put(381, "Z");
		charMap.put(382, "z");
		
		// German
		charMap.put(196, "A");
		charMap.put(228, "a");
		charMap.put(201, "E");
		charMap.put(233, "e");
		charMap.put(214, "O");
		charMap.put(246, "o");
		charMap.put(220, "U");
		charMap.put(252, "u");
		charMap.put(223, "s");
		
		// Spanish
		charMap.put(193, "A");
		charMap.put(225, "a");
		charMap.put(201, "E");
		charMap.put(233, "e");
		charMap.put(205, "I");
		charMap.put(237, "i");
		charMap.put(209, "N");
		charMap.put(241, "n");
		charMap.put(211, "O");
		charMap.put(243, "o");
		charMap.put(218, "U");
		charMap.put(250, "u");
		charMap.put(220, "U");
		charMap.put(252, "u");

		// French
		charMap.put(192, "A");
		charMap.put(224, "a");
		charMap.put(194, "A");
		charMap.put(226, "a");
		charMap.put(198, "A");
		charMap.put(230, "a");
		charMap.put(199, "C");
		charMap.put(231, "c");
		charMap.put(200, "E");
		charMap.put(232, "e");
		charMap.put(201, "E");
		charMap.put(233, "e");
		charMap.put(202, "E");
		charMap.put(234, "e");
		charMap.put(203, "E");
		charMap.put(235, "e");
		charMap.put(206, "I");
		charMap.put(238, "i");
		charMap.put(207, "I");
		charMap.put(239, "i");
		charMap.put(212, "O");
		charMap.put(244, "o");
		charMap.put(140, "O");
		charMap.put(156, "o");
		charMap.put(217, "U");
		charMap.put(249, "u");
		charMap.put(219, "U");
		charMap.put(251, "u");
		charMap.put(220, "U");
		charMap.put(252, "u");
		
		// Dutch
		charMap.put(193, "A");
		charMap.put(201, "E");
		charMap.put(205, "I");
		charMap.put(211, "O");
		charMap.put(218, "U");
		charMap.put(221, "Y");
		charMap.put(225, "a");
		charMap.put(233, "e");
		charMap.put(237, "i");
		charMap.put(243, "o");
		charMap.put(250, "u");
		charMap.put(253, "y");
		charMap.put(196, "A");
		charMap.put(203, "E");
		charMap.put(207, "I");
		charMap.put(214, "O");
		charMap.put(220, "U");
		charMap.put(159, "Y");
		charMap.put(228, "a");
		charMap.put(235, "e");
		charMap.put(239, "i");
		charMap.put(246, "o");
		charMap.put(252, "u");
		charMap.put(255, "Y");
		charMap.put(192, "A");
		charMap.put(200, "E");
		charMap.put(204, "I");
		charMap.put(210, "O");
		charMap.put(217, "U");
		charMap.put(224, "a");
		charMap.put(232, "e");
		charMap.put(236, "i");
		charMap.put(242, "o");
		charMap.put(249, "u");
		charMap.put(194, "A");
		charMap.put(202, "E");
		charMap.put(206, "I");
		charMap.put(212, "O");
		charMap.put(219, "U");
		charMap.put(226, "a");
		charMap.put(234, "e");
		charMap.put(238, "i");
		charMap.put(244, "o");
		charMap.put(251, "u");

		// Portuguese
		charMap.put(192, "A");
		charMap.put(193, "A");
		charMap.put(194, "A");
		charMap.put(195, "A");
		charMap.put(201, "E");
		charMap.put(202, "E");
		charMap.put(205, "I");
		charMap.put(211, "O");
		charMap.put(212, "O");
		charMap.put(213, "O");
		charMap.put(218, "U");
		charMap.put(220, "U");
		charMap.put(224, "a");
		charMap.put(225, "a");
		charMap.put(226, "a");
		charMap.put(227, "a");
		charMap.put(233, "e");
		charMap.put(234, "e");
		charMap.put(237, "i");
		charMap.put(243, "o");
		charMap.put(244, "o");
		charMap.put(245, "o");
		charMap.put(250, "u");
		charMap.put(252, "u");
		charMap.put(199, "C");
		charMap.put(231, "c");
		
		// Russian
		charMap.put(1040, "A");
		charMap.put(1072, "a");
		charMap.put(1041, "B");
		charMap.put(1073, "b");
		charMap.put(1042, "V");
		charMap.put(1074, "v");
		charMap.put(1043, "G");
		charMap.put(1075, "g");
		charMap.put(1044, "D");
		charMap.put(1075, "g");
		charMap.put(1044, "D");
		charMap.put(1076, "d");
		charMap.put(1045, "E");
		charMap.put(1077, "e");
		charMap.put(1046, "Zh");
		charMap.put(1078, "zh");
		charMap.put(1046, "Z");
		charMap.put(1078, "z");
		charMap.put(1047, "Z");
		charMap.put(1079, "z");
		charMap.put(1048, "I");
		charMap.put(1080, "i");
		charMap.put(1049, "J");
		charMap.put(1081, "j");
		charMap.put(1050, "K");
		charMap.put(1082, "k");
		charMap.put(1051, "L");
		charMap.put(1083, "l");
		charMap.put(1052, "M");
		charMap.put(1084, "m");
		charMap.put(1053, "N");
		charMap.put(1085, "n");
		charMap.put(1054, "O");
		charMap.put(1086, "o");
		charMap.put(1055, "P");
		charMap.put(1087, "p");
		charMap.put(1056, "R");
		charMap.put(1088, "r");
		charMap.put(1057, "S");
		charMap.put(1089, "s");
		charMap.put(1058, "T");
		charMap.put(1090, "t");
		charMap.put(1059, "U");
		charMap.put(1091, "u");
		charMap.put(1060, "F");
		charMap.put(1092, "f");
		charMap.put(1061, "X");
		charMap.put(1093, "x");
		charMap.put(1062, "C");
		charMap.put(1094, "c");
		charMap.put(1063, "Ch");
		charMap.put(1095, "ch");
		charMap.put(1064, "Sh");
		charMap.put(1096, "sh");
		charMap.put(1065, "Shh");
		charMap.put(1097, "shh");
		charMap.put(1063, "C");
		charMap.put(1095, "c");
		charMap.put(1064, "S");
		charMap.put(1096, "s");
		charMap.put(1065, "S");
		charMap.put(1097, "s");
		charMap.put(1067, "Y");
		charMap.put(1099, "y");
		charMap.put(1069, "A");
		charMap.put(1101, "a");
		charMap.put(1070, "U");
		charMap.put(1102, "u");
		charMap.put(1071, "Q");
		charMap.put(1103, "q");
	}

	public static String translateCharacter(Character input) {

		if (charMap == null || charMap.size() == 0) {
			init();
		}

		Integer key = (int) input.charValue();

		if (charMap.containsKey(key)) {
			return charMap.get(key);
		} else {
			return "" + input;
		}
	}
}
