object examples {
  val palindrome = """// This is an example program that checks if
                     |// the input string is a binary palindrome
                     |
                     |q0 0 _ r q1o
                     |q0 1 _ r q1i
                     |q0 _ _ r accept
                     |
                     |q1o _ _ l q2o
                     |q1o 1 1 r q1o
                     |q1o 0 0 r q1o
                     |
                     |q1i _ _ l q2i
                     |q1i 0 0 r q1i
                     |q1i 1 1 r q1i
                     |
                     |q2o 0 _ l q3
                     |q2o _ _ r accept
                     |q2o 1 1 r reject
                     |
                     |q2i 1 _ l q3
                     |q2i _ _ r accept
                     |q2i 0 0 r reject
                     |
                     |q3 _ _ r accept
                     |q3 1 1 l q4
                     |q3 0 0 l q4
                     |q4 0 0 l q4
                     |q4 1 1 l q4
                     |q4 _ _ r q0
                     |""".stripMargin

  val anbncn =
    """// This program recognizes strings xyz,
      |// such that x is n consecutive a's,
      |// y is n consecutive b's,
      |// and z is n consecutive c's
      |
      |// the following instructions scan
      |// through the input to verify it is
      |// a string of a's, then b's, then c's
      |// and also prepends $ to the tape
      |
      |q0 a $ r q1
      |
      |q1 a a r q1
      |q1 b a r q2
      |
      |q2 b b r q2
      |q2 c b r q3
      |
      |q3 c c r q3
      |q3 _ c l q4
      |
      |// go back to the beginning
      |q4 a a l q4
      |q4 b b l q4
      |q4 c c l q4
      |q4 $ $ r q5
      |
      |q5 x x r q5
      |q5 a x r q6
      |
      |q6 a a r q6
      |q6 x x r q6
      |q6 b x r q7
      |
      |q7 b b r q7
      |q7 x x r q7
      |q7 c x r q8
      |
      |q8 _ _ l qconfirm
      |q8 c c l q9
      |
      |q9 x x l q9
      |q9 a a l q9
      |q9 b b l q9
      |q9 c c l q9
      |q9 $ $ r q5
      |
      |qconfirm x x l qconfirm
      |qconfirm $ $ l accept
      |
      |""".stripMargin

  val gen =
    """
      |// this non-deterministic program
      |// will output every binary string
      |// equal to the length of the input string
      |
      |qs 1 1 r qs
      |qs 1 0 r qs
      |qs 0 0 r qs
      |qs 0 1 r qs
      |qs _ _ l halt
      |
      |""".stripMargin
}
