--LA-CC 05-135 Trident 0.7.1
--
--Copyright Notice
--Copyright 2006 (c) the Regents of the University of California.
--
--This Software was produced under a U.S. Government contract 
--(W-7405-ENG-36) by Los Alamos National Laboratory, which is operated by 
--the University of California for the U.S. Department of Energy. The U.S. 
--Government is licensed to use, reproduce, and distribute this Software. 
--Permission is granted to the public to copy and use this Software without 
--charge, provided that this Notice and any statement of authorship are 
--reproduced on all copies. Neither the Government nor the University makes 
--any warranty, express or implied, or assumes any liability or 
--responsibility for the user of this Software.


-- this file was written by me by hand
-- I only got this to 189.03592MHz

library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity mux32_fptoi is
  generic (constant pinmap : in integer);
  port(output: out std_logic;
       sel: in std_logic_vector(4 downto 0);
       clk: in std_logic;
       inputs: in std_logic_vector(30 downto 0));
end entity mux32_fptoi;

architecture mux32_fptoi_arch of mux32_fptoi is
    component mux4 is
      port(output: out std_logic;
	   s0: in std_logic;
	   s1: in std_logic;
	   in0: in std_logic;
	   in1: in std_logic;
	   in2: in std_logic;
	   in3: in std_logic);
    end component mux4;
    component mux2 is
      port(output: out std_logic;
	   s0: in std_logic;
	   in0: in std_logic;
	   in1: in std_logic);
    end component mux2;
    signal inputbuffer: std_logic_vector(31 downto 0);
    signal inputbuffertmp: std_logic_vector(31 downto 0);
    signal mux4_0_out: std_logic_vector(0 to 7);
    signal mux4_0_out_reg: std_logic_vector(0 to 7);
    signal mux2_0_out: std_logic_vector(0 to 3);
    signal mux2_0_out_reg: std_logic_vector(0 to 3);
    signal sel_tmp: std_logic_vector(4 downto 0);
    signal sel_0_3: std_logic_vector(4 downto 0);
    signal sel_0_2: std_logic_vector(4 downto 0);
    signal sel_0_1: std_logic_vector(4 downto 0);
    signal sel_0_0: std_logic_vector(4 downto 0);
    signal sel_1: std_logic_vector(4 downto 0);
    signal sel_2: std_logic_vector(4 downto 0);
    constant pin0 : integer := 31-pinmap;
begin
  input_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
      inputbuffertmp(31 downto pin0) <= inputs(pinmap downto 0);
      inputbuffertmp(30-pinmap downto 0) <= (others => '0');
      sel_tmp <= sel;
    end if;
  end process input_reg;
  split_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
      inputbuffer <= inputbuffertmp;
      sel_0_3 <= sel_tmp;
      sel_0_2 <= sel_tmp;
      sel_0_1 <= sel_tmp;
      sel_0_0 <= sel_tmp;
    end if;
  end process split_reg;
  
  mux4_0 : component mux4
    port map(
      output => mux4_0_out(0),
      s0 => sel_0_0(0),
      s1 => sel_0_0(1),
      in0 => inputbuffer(31),
      in1 => inputbuffer(30),
      in2 => inputbuffer(29),
      in3 => inputbuffer(28)
    );
  mux4_1 : component mux4
    port map(
      output => mux4_0_out(1),
      s0 => sel_0_0(0),
      s1 => sel_0_0(1),
      in0 => inputbuffer(27),
      in1 => inputbuffer(26),
      in2 => inputbuffer(25),
      in3 => inputbuffer(24)
    );
  mux4_2 : component mux4
    port map(
      output => mux4_0_out(2),
      s0 => sel_0_1(0),
      s1 => sel_0_1(1),
      in0 => inputbuffer(23),
      in1 => inputbuffer(22),
      in2 => inputbuffer(21),
      in3 => inputbuffer(20)
    );
  mux4_3 : component mux4
    port map(
      output => mux4_0_out(3),
      s0 => sel_0_1(0),
      s1 => sel_0_1(1),
      in0 => inputbuffer(19),
      in1 => inputbuffer(18),
      in2 => inputbuffer(17),
      in3 => inputbuffer(16)
    );
  mux4_4 : component mux4
    port map(
      output => mux4_0_out(4),
      s0 => sel_0_2(0),
      s1 => sel_0_2(1),
      in0 => inputbuffer(15),
      in1 => inputbuffer(14),
      in2 => inputbuffer(13),
      in3 => inputbuffer(12)
    );
  mux4_5 : component mux4
    port map(
      output => mux4_0_out(5),
      s0 => sel_0_2(0),
      s1 => sel_0_2(1),
      in0 => inputbuffer(11),
      in1 => inputbuffer(10),
      in2 => inputbuffer(9),
      in3 => inputbuffer(8)
    );
  mux4_6 : component mux4
    port map(
      output => mux4_0_out(6),
      s0 => sel_0_3(0),
      s1 => sel_0_3(1),
      in0 => inputbuffer(7),
      in1 => inputbuffer(6),
      in2 => inputbuffer(5),
      in3 => inputbuffer(4)
    );
  mux4_7 : component mux4
    port map(
      output => mux4_0_out(7),
      s0 => sel_0_3(0),
      s1 => sel_0_3(1),
      in0 => inputbuffer(3),
      in1 => inputbuffer(2),
      in2 => inputbuffer(1),
      in3 => inputbuffer(0)
    );
    
  middle_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
      mux4_0_out_reg <= mux4_0_out;
      sel_1 <= sel_0_0;
    end if;
  end process middle_reg;

  mux2_0 : component mux2
    port map(
      output => mux2_0_out(0),
      s0 => sel_1(2),
      in0 => mux4_0_out_reg(0),
      in1 => mux4_0_out_reg(1)
    );
  mux2_1 : component mux2
    port map(
      output => mux2_0_out(1),
      s0 => sel_1(2),
      in0 => mux4_0_out_reg(2),
      in1 => mux4_0_out_reg(3)
    );
  mux2_2 : component mux2
    port map(
      output => mux2_0_out(2),
      s0 => sel_1(2),
      in0 => mux4_0_out_reg(4),
      in1 => mux4_0_out_reg(5)
    );
  mux2_3 : component mux2
    port map(
      output => mux2_0_out(3),
      s0 => sel_1(2),
      in0 => mux4_0_out_reg(6),
      in1 => mux4_0_out_reg(7)
    );

  last_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
      mux2_0_out_reg <= mux2_0_out;
      sel_2 <= sel_1;
    end if;
  end process last_reg;

  mux4_8 : component mux4
    port map(
      output => output,
      s0 => sel_2(3),
      s1 => sel_2(4),
      in0 => mux2_0_out_reg(0),
      in1 => mux2_0_out_reg(1),
      in2 => mux2_0_out_reg(2),
      in3 => mux2_0_out_reg(3)
    );
  
end architecture mux32_fptoi_arch;

library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity log_shift_left_32_fptoi is
  port(data_in: in std_logic_vector(30 downto 0);
       shift_cnt: in std_logic_vector(4 downto 0);
       data_out: out std_logic_vector(30 downto 0);
       clk: in std_logic);
end entity log_shift_left_32_fptoi;

architecture log_shift_left_32_arch of log_shift_left_32_fptoi is
   component mux32_fptoi is
     generic (constant pinmap : in integer);
     port(output: out std_logic;
          sel: in std_logic_vector(4 downto 0);
	  clk: in std_logic;
          inputs: in std_logic_vector(30 downto 0));
   end component mux32_fptoi;
   signal out_buffer: std_logic_vector(30 downto 0);
   signal input_buffer: std_logic_vector(30 downto 0);
   signal input_buffer_0: std_logic_vector(30 downto 0);
   signal input_buffer_1: std_logic_vector(30 downto 0);
   signal input_buffer_2: std_logic_vector(30 downto 0);
   signal input_buffer_3: std_logic_vector(30 downto 0);
   signal shift_cnt_save: std_logic_vector(4 downto 0);
   signal shift_cnt_save_0: std_logic_vector(4 downto 0);
   signal shift_cnt_save_1: std_logic_vector(4 downto 0);
   signal shift_cnt_0: std_logic_vector(4 downto 0);
   signal shift_cnt_1: std_logic_vector(4 downto 0);
   signal shift_cnt_2: std_logic_vector(4 downto 0);
   signal shift_cnt_3: std_logic_vector(4 downto 0);
   --signal z_net: std_logic_vector(31 downto 0);
   --signal buffer2: std_logic_vector(5 downto 0);
   --signal buffer3: std_logic_vector(30 downto 0);
begin
  input: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
	input_buffer <= data_in;
	shift_cnt_save_0 <= shift_cnt;
	shift_cnt_save_1 <= shift_cnt;
      end if;
  end process input;
  divide_data: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
	input_buffer_0 <= input_buffer;
	input_buffer_1 <= input_buffer;
	input_buffer_2 <= input_buffer;
	input_buffer_3 <= input_buffer;
	shift_cnt_0 <= shift_cnt_save_0;
	shift_cnt_1 <= shift_cnt_save_0;
	shift_cnt_2 <= shift_cnt_save_1;
	shift_cnt_3 <= shift_cnt_save_1;
      end if;
  end process divide_data;

  foreachpin_0: for pin0 in 0 to 7 generate
   --constant middlePoint: integer := 30 - pin0; 
   --constant middlePoint1: integer := 30 - pin0 - 1; 
  begin
    --foreachpin1: for pin1 in pin0 downto pin0 - data_in'high generate
    --begin
      --mappin: if pin0 - data_in'high >= 0 generate
      --begin
	--z_net <= (others => '0');
	mux32_shift: component mux32_fptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(30 downto 0) => 
		     	  input_buffer_0(30 downto 0),
	             sel => shift_cnt_0);
		     --inputs(middlePoint1 downto 0) => 
		     --	  z_net(middlePoint1 downto 0));
		     --inputs => data_in(pin0 downto 0) & (others => '0'));
      --end generate mappin;
    --end generate foreachpin1;
  end generate foreachpin_0;
  foreachpin_1: for pin0 in 8 to 15 generate
  begin
	mux32_shift: component mux32_fptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(30 downto 0) => 
		     	  input_buffer_1(30 downto 0),
	             sel => shift_cnt_1);
  end generate foreachpin_1;
  foreachpin_2: for pin0 in 16 to 23 generate
  begin
	mux32_shift: component mux32_fptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(30 downto 0) => 
		     	  input_buffer_2(30 downto 0),
	             sel => shift_cnt_2);
  end generate foreachpin_2;
  foreachpin_3: for pin0 in 24 to 30 generate
  begin
	mux32_shift: component mux32_fptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(30 downto 0) => 
		     	  input_buffer_3(30 downto 0),
	             sel => shift_cnt_3);
  end generate foreachpin_3;
  store_data: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
        data_out <= out_buffer;
  --data_out <= out_buffer when i_start = '1' else (others => '0');
      end if;
  end process store_data;
  
end architecture log_shift_left_32_arch;

library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity fp_to_i is
  port(vec_int: out std_logic_vector(31 downto 0);
       vec_fp: in std_logic_vector(31 downto 0);
       clk: in std_logic);
end entity fp_to_i;

architecture behavioural of fp_to_i is

--======================================================--
--                     FUNCTIONS                        --
--======================================================--
    --function to exor second, standard logic input with each element in 1st std_logic_vector input
    function exor(input_vec: std_logic_vector; input_logic: std_logic)
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input_vec'high downto 0); 
    begin
    	    calc: for i in 0 to input_vec'high loop
    	      ans(i) := (input_vec(i) and not input_logic) or 
    			(not input_vec(i) and input_logic);
    	    end loop calc;
    	    return ans; 
    end function exor;
    --function to combine the contents of two vectors 
    function combine2 (input1: std_logic_vector; input0: std_logic_vector)
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input1'high + input0'high downto 0); 
    begin
    	    ans(input1'high + input0'high downto input0'high + 1) := input1;
    	    ans(input0'high + 1 downto 0) := input0;
    	    return ans; 
    end function combine2;
    --function to combine the contents of four vectors 
    function combine4 (input3: std_logic_vector; input2: std_logic_vector;
    		       input1: std_logic_vector; input0: std_logic_vector)
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input3'high + input2'high + input1'high + input0'high  downto 0); 
    begin
    	    ans(input3'high + input2'high + input1'high + input0'high downto input2'high + input1'high + input0'high + 1) := input3;
    	    ans(input2'high + input1'high + input0'high downto input1'high + input0'high + 1) := input2;
    	    ans(input1'high + input0'high downto input0'high + 1) := input1;
    	    ans(input0'high + 1 downto 0) := input0;
    	    return ans; 
    end function combine4;
    --function 
    function break_apart_t_1_bit (input: std_logic_vector) 
    		  return std_logic is
    	    variable ans : std_logic; 
    begin
    		ans := input(1);
    	    return ans; 
    end function break_apart_t_1_bit;
    function break_apart_b_1_bit (input: std_logic_vector) 
    		  return std_logic is
    	    variable ans : std_logic; 
    begin
    		ans := input(0);
    	    return ans; 
    end function break_apart_b_1_bit;
    function break_apart_0_1_bit (input: std_logic_vector) 
    		  return std_logic is
    	    variable ans : std_logic; 
    begin
    		ans := input(0);
    	    return ans; 
    end function break_apart_0_1_bit;
    function break_apart_1_1_bit (input: std_logic_vector) 
    		  return std_logic is
    	    variable ans : std_logic; 
    begin
    		ans := input(1);
    	    return ans; 
    end function break_apart_1_1_bit;
    function break_apart_2_1_bit (input: std_logic_vector) 
    		  return std_logic is
    	    variable ans : std_logic; 
    begin
    		ans := input(2);
    	    return ans; 
    end function break_apart_2_1_bit;
    function break_apart_3_1_bit (input: std_logic_vector) 
    		  return std_logic is
    	    variable ans : std_logic; 
    begin
    		ans := input(3);
    	    return ans; 
    end function break_apart_3_1_bit;
    --function 
    function break_apart_3 (input: std_logic_vector) 
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input'high/4 downto 0); 
    	    variable cntr : integer := input'high/4;
    begin
    	    break_up: for i in input'high downto 3*input'high/4+1 loop
    		ans(cntr) := input(i);
    		cntr := cntr - 1;
    	    end loop break_up;
    	    return ans; 
    end function break_apart_3;
    --function 
    function break_apart_2 (input: std_logic_vector) 
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input'high/4 downto 0); 
    	    variable cntr : integer := input'high/4;
    begin
    	    break_up: for i in 3*input'high/4 downto input'high/2+1 loop
    		ans(cntr) := input(i);
    		cntr := cntr - 1;
    	    end loop break_up;
    	    return ans; 
    end function break_apart_2;
    --function 
    function break_apart_1 (input: std_logic_vector) 
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input'high/4 downto 0); 
    	    variable cntr : integer := input'high/4;
    begin
    	    break_up: for i in input'high/2 downto input'high/4+1 loop
    		ans(cntr) := input(i);
    		cntr := cntr - 1;
    	    end loop break_up;
    	    return ans; 
    end function break_apart_1;
    --function 
    function break_apart_0 (input: std_logic_vector) 
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(input'high/4 downto 0); 
    	    variable cntr : integer := input'high/4;
    begin
    	    break_up: for i in input'high/4 downto 0 loop
    		ans(cntr) := input(i);
    		cntr := cntr - 1;
    	    end loop break_up;
    	    return ans; 
    end function break_apart_0;
    
    --function to invert the bits in a std_logic_vector
    function invert_bits (vector: std_logic_vector) 
    		  return std_logic_vector is
    	    variable ans : std_logic_vector(vector'high downto vector'low); 
    	    variable cnt : integer := 0;
    begin
    	    conv: for i in vector'low to vector'high loop
    	      ans(cnt) := vector(i);
    	      cnt := cnt + 1;
    	    end loop conv;
    	    return ans; 
    end function invert_bits;

    component log_shift_left_32_fptoi is
      port(data_in: in std_logic_vector(30 downto 0);
           shift_cnt: in std_logic_vector(4 downto 0);
           data_out: out std_logic_vector(30 downto 0);
	   clk: in std_logic);
    end component log_shift_left_32_fptoi;

    signal mant: std_logic_vector(30 downto 0);
    signal mant1: std_logic_vector(30 downto 0);
    --signal mant_tmp: std_logic_vector(30 downto 0);
    --signal mant_tmp2: std_logic_vector(30 downto 0);
    signal sine: std_logic;
    signal sine_save0: std_logic;
    signal sine_save1: std_logic;
    signal sine_save2: std_logic;
    signal sine_save3: std_logic;
    signal sine_save3_0: std_logic;
    signal sine_save3_1: std_logic;
    signal sine_save4: std_logic;
    signal sine_save5: std_logic;
    signal sine_save6: std_logic;
    signal sine_save7: std_logic;
    signal sine_save8: std_logic;
    signal exp: std_logic_vector(7 downto 0);
    signal exp_save0: std_logic_vector(7 downto 0);
    signal exp_save0_0: std_logic_vector(7 downto 0);
    signal exp_save0_0_plus_one: std_logic_vector(7 downto 0);
    signal exp_save0_0_0: std_logic;
    signal exp_save0_0_1: std_logic_vector(1 downto 0);
    signal exp_save0_0_2: std_logic_vector(2 downto 0);
    signal exp_save0_0_3: std_logic_vector(3 downto 0);
    signal exp_save0_0_4: std_logic_vector(4 downto 0);
    signal exp_save0_0_5: std_logic_vector(5 downto 0);
    signal exp_save0_0_6: std_logic_vector(6 downto 0);
    signal exp_save0_1: std_logic_vector(7 downto 0);
    signal exp_save0_2: std_logic_vector(7 downto 0);
    signal exp_save1: std_logic_vector(7 downto 0);
    signal exp_save2: std_logic_vector(7 downto 0);
    signal exp_save3: std_logic_vector(7 downto 0);
    signal exp_save3_0: std_logic_vector(7 downto 0);
    signal exp_save3_1: std_logic_vector(7 downto 0);
    signal exp_save4: std_logic_vector(7 downto 0);
    signal exp_save4_0: std_logic_vector(7 downto 0);
    signal exp_save4_1: std_logic_vector(7 downto 0);
    signal exp_save4_2: std_logic_vector(7 downto 0);
    signal exp_save4_3: std_logic_vector(7 downto 0);
    --signal exp_save5: std_logic_vector(7 downto 0);
    signal exp_save5_0: std_logic_vector(7 downto 0);
    signal exp_save5_1: std_logic_vector(7 downto 0);
    signal exp_save5_2: std_logic_vector(7 downto 0);
    signal exp_save5_3: std_logic_vector(7 downto 0);
    signal exp_save5_4: std_logic_vector(7 downto 0);
    signal exp_save5_5: std_logic_vector(7 downto 0);
    signal exp_save5_6: std_logic_vector(7 downto 0);
    signal exp_save5_7: std_logic_vector(7 downto 0);
    --signal exp_save6: std_logic_vector(7 downto 0);
    --signal exp_save7: std_logic_vector(7 downto 0);
    --signal exp_save8: std_logic_vector(7 downto 0);
    --signal exp_save9: std_logic_vector(7 downto 0);
    --signal exp_save10: std_logic_vector(7 downto 0);
   -- signal exp_save11: std_logic_vector(7 downto 0);
    signal int_tmp: std_logic_vector(30 downto 0);
    signal int_tmp_0: std_logic_vector(30 downto 0);
    signal int_tmp_inv: std_logic_vector(31 downto 0);
    signal int_tmp_inv_0: std_logic_vector(31 downto 0);
    --signal int_tmp2: std_logic_vector(31 downto 0);
    --signal int_tmp3: std_logic_vector(30 downto 0);
    --signal int_tmp4: std_logic_vector(31 downto 0);
    signal exp_to_shift: std_logic_vector(7 downto 0);
    signal vec_int_tmp: std_logic_vector(31 downto 0);
    signal vec_int_tmp_0: std_logic_vector(31 downto 0);
    signal vec_int_tmp_1: std_logic_vector(31 downto 0);
    signal vec_int_tmp_2: std_logic_vector(31 downto 0);
    signal vec_int_tmp_3: std_logic_vector(31 downto 0);
    signal vec_int_tmp_4: std_logic_vector(31 downto 0);
    signal vec_int_tmp_5: std_logic_vector(31 downto 0);
    signal vec_int_final: std_logic_vector(31 downto 0);
    signal vec_int_tmp_final: std_logic_vector(31 downto 0);
    signal vec_int_tmp_0_reg: std_logic;
    signal vec_int_tmp_1_reg: std_logic_vector(1 downto 0);
    signal vec_int_tmp_2_reg: std_logic_vector(2 downto 0);
    signal vec_int_tmp_3_reg: std_logic_vector(3 downto 0);
    signal vec_int_tmp_4_reg: std_logic_vector(4 downto 0);
    signal vec_int_tmp_5_reg: std_logic_vector(5 downto 0);
    signal vec_int_tmp_6_reg: std_logic_vector(6 downto 0);
    signal vec_int_tmp_7_reg: std_logic_vector(7 downto 0);
    signal vec_int_tmp_8_reg: std_logic_vector(8 downto 0);
    signal vec_int_tmp_9_reg: std_logic_vector(9 downto 0);
    signal vec_int_tmp_10_reg: std_logic_vector(10 downto 0);
    signal vec_int_tmp_11_reg: std_logic_vector(11 downto 0);
    signal vec_int_tmp_12_reg: std_logic_vector(12 downto 0);
    signal vec_int_tmp_13_reg: std_logic_vector(13 downto 0);
    signal vec_int_tmp_14_reg: std_logic_vector(14 downto 0);
    signal vec_int_tmp_15_reg: std_logic_vector(15 downto 0);
    signal vec_int_tmp_16_reg: std_logic_vector(16 downto 0);
    signal vec_int_tmp_17_reg: std_logic_vector(17 downto 0);
    signal vec_int_tmp_18_reg: std_logic_vector(18 downto 0);
    signal vec_int_tmp_19_reg: std_logic_vector(19 downto 0);
    signal vec_int_tmp_20_reg: std_logic_vector(20 downto 0);
    signal vec_int_tmp_21_reg: std_logic_vector(21 downto 0);
    signal vec_int_tmp_22_reg: std_logic_vector(22 downto 0);
    signal vec_int_tmp_23_reg: std_logic_vector(23 downto 0);
    signal vec_int_tmp_24_reg: std_logic_vector(24 downto 0);
    signal vec_int_tmp_25_reg: std_logic_vector(25 downto 0);
    signal vec_int_tmp_26_reg: std_logic_vector(26 downto 0);
    signal vec_int_tmp_27_reg: std_logic_vector(27 downto 0);
    signal vec_int_tmp_28_reg: std_logic_vector(28 downto 0);
    signal vec_int_tmp_29_reg: std_logic_vector(29 downto 0);
    signal vec_int_tmp_30_reg: std_logic_vector(30 downto 0);

    --2's compliment stuff
    signal bit_1_block_0_final_0: std_logic;
    signal bit_1_block_0_final_1: std_logic;
    signal bit_1_block_0_final_2: std_logic;
    signal bit_1_block_0_final_3: std_logic;
    signal bit_1_block_1_final_0: std_logic;
    signal bit_1_block_1_final_1: std_logic;
    signal bit_1_block_1_final_2: std_logic;
    signal bit_1_block_1_final_3: std_logic;
    signal bit_2_block_0_final_0: std_logic_vector(1 downto 0);
    signal bit_2_block_0_final_1: std_logic_vector(1 downto 0);
    signal bit_2_block_0_final_2: std_logic_vector(1 downto 0);
    signal bit_2_block_0_final_3: std_logic_vector(1 downto 0);
    signal bit_2_block_1_final_0: std_logic_vector(1 downto 0);
    signal bit_2_block_1_final_1: std_logic_vector(1 downto 0);
    signal bit_2_block_1_final_2: std_logic_vector(1 downto 0);
    signal bit_2_block_1_final_3: std_logic_vector(1 downto 0);
    signal bit_2_block_2_final_0: std_logic_vector(1 downto 0);
    signal bit_2_block_2_final_1: std_logic_vector(1 downto 0);
    signal bit_2_block_2_final_2: std_logic_vector(1 downto 0);
    signal bit_2_block_2_final_3: std_logic_vector(1 downto 0);
    signal bit_2_block_3_final_0: std_logic_vector(1 downto 0);
    signal bit_2_block_3_final_1: std_logic_vector(1 downto 0);
    signal bit_2_block_3_final_2: std_logic_vector(1 downto 0);
    signal bit_2_block_3_final_3: std_logic_vector(1 downto 0);
    
    
    signal bit_1_block_0: std_logic;
    signal bit_1_block_0_final: std_logic;
    signal bit_1_block_0_new: std_logic;
    signal bit_1_block_0_new_reg_1: std_logic;
    signal bit_1_block_0_reg: std_logic;
    signal bit_1_block_0_zero_flag: std_logic;
    signal bit_1_block_0_zero_flag_reg: std_logic;
    signal bit_1_block_0_zero_flag_reg_1: std_logic;
    signal bit_1_block_0_zero_flag_reg_2: std_logic;
    signal bit_1_block_1: std_logic;
    signal bit_1_block_1_final: std_logic;
    signal bit_1_block_1_new: std_logic;
    signal bit_1_block_1_new_reg_1: std_logic;
    signal bit_1_block_1_reg: std_logic;
    signal bit_1_block_next: std_logic;
    signal bit_1_block_next_reg: std_logic;
    signal bit_2_block_0: std_logic_vector(1 downto 0);
    signal bit_2_block_0_final: std_logic_vector(1 downto 0);
    signal bit_2_block_0_new: std_logic_vector(1 downto 0);
    signal bit_2_block_0_new_reg_1: std_logic_vector(1 downto 0);
    signal bit_2_block_0_new_reg_2: std_logic_vector(1 downto 0);
    signal bit_2_block_0_new_reg_3: std_logic_vector(1 downto 0);
    signal bit_2_block_0_reg: std_logic_vector(1 downto 0);
    signal bit_2_block_0_zero_flag: std_logic;
    signal bit_2_block_0_zero_flag_reg: std_logic;
    signal bit_2_block_0_zero_flag_reg_1: std_logic;
    signal bit_2_block_0_zero_flag_reg_2: std_logic;
    signal bit_2_block_0_zero_flag_reg_3: std_logic;
    signal bit_2_block_0_zero_flag_reg_4: std_logic;
    signal bit_2_block_1: std_logic_vector(1 downto 0);
    signal bit_2_block_1_final: std_logic_vector(1 downto 0);
    signal bit_2_block_1_new: std_logic_vector(1 downto 0);
    signal bit_2_block_1_new_reg_1: std_logic_vector(1 downto 0);
    signal bit_2_block_1_new_reg_2: std_logic_vector(1 downto 0);
    signal bit_2_block_1_new_reg_3: std_logic_vector(1 downto 0);
    signal bit_2_block_1_reg: std_logic_vector(1 downto 0);
    signal bit_2_block_1_zero_flag: std_logic;
    signal bit_2_block_1_zero_flag_reg: std_logic;
    signal bit_2_block_1_zero_flag_reg_1: std_logic;
    signal bit_2_block_1_zero_flag_reg_2: std_logic;
    signal bit_2_block_1_zero_flag_reg_3: std_logic;
    signal bit_2_block_1_zero_flag_reg_4: std_logic;
    signal bit_2_block_2: std_logic_vector(1 downto 0);
    signal bit_2_block_2_final: std_logic_vector(1 downto 0);
    signal bit_2_block_2_new: std_logic_vector(1 downto 0);
    signal bit_2_block_2_new_reg_1: std_logic_vector(1 downto 0);
    signal bit_2_block_2_new_reg_2: std_logic_vector(1 downto 0);
    signal bit_2_block_2_new_reg_3: std_logic_vector(1 downto 0);
    signal bit_2_block_2_reg: std_logic_vector(1 downto 0);
    signal bit_2_block_2_zero_flag: std_logic;
    signal bit_2_block_2_zero_flag_reg: std_logic;
    signal bit_2_block_2_zero_flag_reg_1: std_logic;
    signal bit_2_block_2_zero_flag_reg_2: std_logic;
    signal bit_2_block_2_zero_flag_reg_3: std_logic;
    signal bit_2_block_2_zero_flag_reg_4: std_logic;
    signal bit_2_block_3: std_logic_vector(1 downto 0);
    signal bit_2_block_3_final: std_logic_vector(1 downto 0);
    signal bit_2_block_3_new: std_logic_vector(1 downto 0);
    signal bit_2_block_3_new_reg_1: std_logic_vector(1 downto 0);
    signal bit_2_block_3_new_reg_2: std_logic_vector(1 downto 0);
    signal bit_2_block_3_new_reg_3: std_logic_vector(1 downto 0);
    signal bit_2_block_3_reg: std_logic_vector(1 downto 0);
    signal bit_2_block_next: std_logic_vector(1 downto 0);
    signal bit_2_block_next_reg: std_logic_vector(1 downto 0);
    signal bit_32_block_0_final: std_logic_vector(31 downto 0);
    signal bit_32_block_next_reg: std_logic_vector(31 downto 0);
    signal bit_8_block_0: std_logic_vector(7 downto 0);
    signal bit_8_block_0_final: std_logic_vector(7 downto 0);
    signal bit_8_block_0_new: std_logic_vector(7 downto 0);
    signal bit_8_block_0_new_reg_1: std_logic_vector(7 downto 0);
    signal bit_8_block_0_new_reg_2: std_logic_vector(7 downto 0);
    signal bit_8_block_0_new_reg_3: std_logic_vector(7 downto 0);
    signal bit_8_block_0_new_reg_4: std_logic_vector(7 downto 0);
    signal bit_8_block_0_new_reg_5: std_logic_vector(7 downto 0);
    signal bit_8_block_0_reg: std_logic_vector(7 downto 0);
    signal bit_8_block_0_zero_flag: std_logic;
    signal bit_8_block_0_zero_flag_reg: std_logic;
    signal bit_8_block_0_zero_flag_reg_1: std_logic;
    signal bit_8_block_0_zero_flag_reg_2: std_logic;
    signal bit_8_block_0_zero_flag_reg_3: std_logic;
    signal bit_8_block_0_zero_flag_reg_4: std_logic;
    signal bit_8_block_0_zero_flag_reg_5: std_logic;
    signal bit_8_block_0_zero_flag_reg_6: std_logic;
    signal bit_8_block_1: std_logic_vector(7 downto 0);
    signal bit_8_block_1_final: std_logic_vector(7 downto 0);
    signal bit_8_block_1_new: std_logic_vector(7 downto 0);
    signal bit_8_block_1_new_reg_1: std_logic_vector(7 downto 0);
    signal bit_8_block_1_new_reg_2: std_logic_vector(7 downto 0);
    signal bit_8_block_1_new_reg_3: std_logic_vector(7 downto 0);
    signal bit_8_block_1_new_reg_4: std_logic_vector(7 downto 0);
    signal bit_8_block_1_new_reg_5: std_logic_vector(7 downto 0);
    signal bit_8_block_1_reg: std_logic_vector(7 downto 0);
    signal bit_8_block_1_zero_flag: std_logic;
    signal bit_8_block_1_zero_flag_reg: std_logic;
    signal bit_8_block_1_zero_flag_reg_1: std_logic;
    signal bit_8_block_1_zero_flag_reg_2: std_logic;
    signal bit_8_block_1_zero_flag_reg_3: std_logic;
    signal bit_8_block_1_zero_flag_reg_4: std_logic;
    signal bit_8_block_1_zero_flag_reg_5: std_logic;
    signal bit_8_block_1_zero_flag_reg_6: std_logic;
    signal bit_8_block_2: std_logic_vector(7 downto 0);
    signal bit_8_block_2_final: std_logic_vector(7 downto 0);
    signal bit_8_block_2_new: std_logic_vector(7 downto 0);
    signal bit_8_block_2_new_reg_1: std_logic_vector(7 downto 0);
    signal bit_8_block_2_new_reg_2: std_logic_vector(7 downto 0);
    signal bit_8_block_2_new_reg_3: std_logic_vector(7 downto 0);
    signal bit_8_block_2_new_reg_4: std_logic_vector(7 downto 0);
    signal bit_8_block_2_new_reg_5: std_logic_vector(7 downto 0);
    signal bit_8_block_2_reg: std_logic_vector(7 downto 0);
    signal bit_8_block_2_zero_flag: std_logic;
    signal bit_8_block_2_zero_flag_reg: std_logic;
    signal bit_8_block_2_zero_flag_reg_1: std_logic;
    signal bit_8_block_2_zero_flag_reg_2: std_logic;
    signal bit_8_block_2_zero_flag_reg_3: std_logic;
    signal bit_8_block_2_zero_flag_reg_4: std_logic;
    signal bit_8_block_2_zero_flag_reg_5: std_logic;
    signal bit_8_block_2_zero_flag_reg_6: std_logic;
    signal bit_8_block_3: std_logic_vector(7 downto 0);
    signal bit_8_block_3_final: std_logic_vector(7 downto 0);
    signal bit_8_block_3_new: std_logic_vector(7 downto 0);
    signal bit_8_block_3_new_reg_1: std_logic_vector(7 downto 0);
    signal bit_8_block_3_new_reg_2: std_logic_vector(7 downto 0);
    signal bit_8_block_3_new_reg_3: std_logic_vector(7 downto 0);
    signal bit_8_block_3_new_reg_4: std_logic_vector(7 downto 0);
    signal bit_8_block_3_new_reg_5: std_logic_vector(7 downto 0);
    signal bit_8_block_3_reg: std_logic_vector(7 downto 0);
    signal bit_8_block_next: std_logic_vector(7 downto 0);
    signal bit_8_block_next_reg: std_logic_vector(7 downto 0);
    signal conv_sin_reg_0: std_logic;
    signal conv_sin_reg_1: std_logic;
    signal conv_sin_reg_2: std_logic;
    signal conv_sin_reg_3: std_logic;
    signal conv_sin_reg_4: std_logic;
    signal conv_sin_reg_5: std_logic;
    signal conv_sin_reg_6: std_logic;

begin
  
  sine <= vec_fp(31);
  exp <= vec_fp(30 downto 23) - b"10111";
  mant <= x"01" & vec_fp(22 downto 0) when exp(7) = '1' else
          invert_bits(x"01" & vec_fp(22 downto 0));
  --mant_tmp <= invert_bits(x"01" & vec_fp(22 downto 0));
  --mant_tmp2 <= x"01" & vec_fp(22 downto 0);

  trans_0: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save0 <= exp;
      exp_save0_0 <= exp;
      exp_save0_0_0 <= exp(0);
      exp_save0_0_1 <= exp(1 downto 0);
      exp_save0_0_2 <= exp(2 downto 0);
      exp_save0_0_3 <= exp(3 downto 0);
      exp_save0_0_4 <= exp(4 downto 0);
      exp_save0_0_5 <= exp(5 downto 0);
      exp_save0_0_6 <= exp(6 downto 0);
      exp_save0_1 <= exp;
      exp_save0_2 <= exp;
      sine_save0<= sine;
      mant1 <= mant;
    end if;
  end process trans_0;

  exp_save0_0_plus_one(0) <= not exp_save0_0(0);
  exp_save0_0_plus_one(1) <= not exp_save0_0(1) when exp_save0_0_0 = '1' else
                             exp_save0_0(1);
  exp_save0_0_plus_one(2) <= not exp_save0_0(2) when exp_save0_0_1 = b"11" else
                             exp_save0_0(2);
  exp_save0_0_plus_one(3) <= not exp_save0_0(3) when exp_save0_0_2 = b"111" else
                             exp_save0_0(3);
  exp_save0_0_plus_one(4) <= not exp_save0_0(4) when exp_save0_0_3 = b"1111" else
                             exp_save0_0(4);
  exp_save0_0_plus_one(5) <= not exp_save0_0(5) when exp_save0_0_4 = b"11111" else
                             exp_save0_0(5);
  exp_save0_0_plus_one(6) <= not exp_save0_0(6) when exp_save0_0_5 = b"111111" else
                             exp_save0_0(6);
  exp_save0_0_plus_one(7) <= not exp_save0_0(7) when exp_save0_0_6 = b"1111111" else
                             exp_save0_0(7);

  exp_to_shift <= exp_save0_0_plus_one when exp_save0_1(7) = '1' else
                  not exp_save0_2;

  
  trans_1: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save1 <= exp_save0;
      sine_save1 <= sine_save0;
    end if;
  end process trans_1;
  trans_2: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save2 <= exp_save1;
      sine_save2 <= sine_save1;
    end if;
  end process trans_2;
  trans_3: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save3 <= exp_save2;
      sine_save3 <= sine_save2;
    end if;
  end process trans_3;
  trans_3_1: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save3_0 <= exp_save3;
      sine_save3_0 <= sine_save3;
    end if;
  end process trans_3_1;
  trans_3_2: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save3_1 <= exp_save3_0;
      sine_save3_1 <= sine_save3_0;
    end if;
  end process trans_3_2;
  trans_4: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save4 <= exp_save3_1;
      sine_save4 <= sine_save3_1;
    end if;
  end process trans_4;
  trans_5: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save4_0 <= exp_save4;
      exp_save4_1 <= exp_save4;
      sine_save5 <= sine_save4;
    end if;
  end process trans_5;
  trans_6: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save5_0 <= exp_save4_0;
      exp_save5_1 <= exp_save4_0;
      exp_save5_2 <= exp_save4_0;
      exp_save5_3 <= exp_save4_0;
      exp_save5_4 <= exp_save4_1;
      exp_save5_5 <= exp_save4_1;
      exp_save5_6 <= exp_save4_1;
      exp_save5_7 <= exp_save4_1;
      sine_save6 <= sine_save5;
      int_tmp <= int_tmp_0;
      int_tmp_inv <= int_tmp_inv_0;
    end if;
  end process trans_6;
  shftlftlog_32_fptoi : component log_shift_left_32_fptoi
    port map(
      data_in => mant1, 
      data_out => int_tmp_0,
      shift_cnt => exp_to_shift(4 downto 0),
      clk => clk
    );
  int_tmp_inv_0 <= invert_bits(int_tmp_0 & '0');
  save_mid_result: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      --if exp_save5(6 downto 5) /= b"00" then --in this case we have over or underflow
      --  vec_int <= x"00000000";          --how do we make it crash?
      --elsif exp_save5(7) = '1' then
      --vec_int(31) <= sine_save6;
      sine_save7 <= sine_save6;
      if exp_save5_0(7) = '1' then
        vec_int_tmp(3 downto 0) <= int_tmp(3 downto 0);
      else
        vec_int_tmp(3 downto 0) <= int_tmp_inv(3 downto 0);
      end if;
      if exp_save5_1(7) = '1' then
        vec_int_tmp(7 downto 4) <= int_tmp(7 downto 4);
      else
        vec_int_tmp(7 downto 4) <= int_tmp_inv(7 downto 4);
      end if;
      if exp_save5_2(7) = '1' then
        vec_int_tmp(11 downto 8) <= int_tmp(11 downto 8);
      else
        vec_int_tmp(11 downto 8) <= int_tmp_inv(11 downto 8);
      end if;
      if exp_save5_3(7) = '1' then
        vec_int_tmp(15 downto 12) <= int_tmp(15 downto 12);
      else
        vec_int_tmp(15 downto 12) <= int_tmp_inv(15 downto 12);
      end if;
      if exp_save5_4(7) = '1' then
        vec_int_tmp(19 downto 16) <= int_tmp(19 downto 16);
      else
        vec_int_tmp(19 downto 16) <= int_tmp_inv(19 downto 16);
      end if;
      if exp_save5_5(7) = '1' then
        vec_int_tmp(23 downto 20) <= int_tmp(23 downto 20);
      else
        vec_int_tmp(23 downto 20) <= int_tmp_inv(23 downto 20);
      end if;
      if exp_save5_6(7) = '1' then
        vec_int_tmp(27 downto 24) <= int_tmp(27 downto 24);
      else
        vec_int_tmp(27 downto 24) <= int_tmp_inv(27 downto 24);
      end if;
      if exp_save5_7(7) = '1' then
        vec_int_tmp(30 downto 28) <= int_tmp(30 downto 28);
      else
        vec_int_tmp(30 downto 28) <= int_tmp_inv(30 downto 28);
      end if;
    end if;
  end process save_mid_result;
  
  
  bit_32_block_next_reg <= vec_int_tmp;
  conv_sin_reg_0 <= sine_save7;


  bit_8_block_0 <= break_apart_0(bit_32_block_next_reg);
  bit_8_block_1 <= break_apart_1(bit_32_block_next_reg);
  bit_8_block_2 <= break_apart_2(bit_32_block_next_reg);
  bit_8_block_3 <= break_apart_3(bit_32_block_next_reg);


  bit_8_block_0_zero_flag <= not ( bit_8_block_0(0) or 
	  	 	 	   bit_8_block_0(1) or 
	  	 	 	   bit_8_block_0(2) or 
	  	 	 	   bit_8_block_0(3) or 
	  	 	 	   bit_8_block_0(4) or 
	  	 	 	   bit_8_block_0(5) or 
	  	 	 	   bit_8_block_0(6) or 
	  	 	 	   bit_8_block_0(7)); 
  bit_8_block_1_zero_flag <= not ( bit_8_block_1(0) or 
	  	 	 	   bit_8_block_1(1) or 
	  	 	 	   bit_8_block_1(2) or 
	  	 	 	   bit_8_block_1(3) or 
	  	 	 	   bit_8_block_1(4) or 
	  	 	 	   bit_8_block_1(5) or 
	  	 	 	   bit_8_block_1(6) or 
	  	 	 	   bit_8_block_1(7)); 
  bit_8_block_2_zero_flag <= not ( bit_8_block_2(0) or 
	  	 	 	   bit_8_block_2(1) or 
	  	 	 	   bit_8_block_2(2) or 
	  	 	 	   bit_8_block_2(3) or 
	  	 	 	   bit_8_block_2(4) or 
	  	 	 	   bit_8_block_2(5) or 
	  	 	 	   bit_8_block_2(6) or 
	  	 	 	   bit_8_block_2(7)); 


  conv_to_non_neg_0: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_8_block_0_zero_flag_reg <= bit_8_block_0_zero_flag;
      bit_8_block_0_reg <= bit_8_block_0;
      bit_8_block_1_zero_flag_reg <= bit_8_block_1_zero_flag;
      bit_8_block_1_reg <= bit_8_block_1;
      bit_8_block_2_zero_flag_reg <= bit_8_block_2_zero_flag;
      bit_8_block_2_reg <= bit_8_block_2;
      bit_8_block_3_reg <= bit_8_block_3;
      conv_sin_reg_1 <= conv_sin_reg_0;
      bit_8_block_0_zero_flag_reg_1 <= bit_8_block_0_zero_flag;
      bit_8_block_1_zero_flag_reg_1 <= bit_8_block_1_zero_flag;
      bit_8_block_2_zero_flag_reg_1 <= bit_8_block_2_zero_flag;
      vec_int_tmp_0 <= vec_int_tmp;
    end if;
  end process conv_to_non_neg_0;


  bit_8_block_0_new <= bit_8_block_0_reg;
  bit_8_block_1_new <= bit_8_block_1_reg when (bit_8_block_0_zero_flag_reg='1') else 
		       exor(bit_8_block_1_reg, conv_sin_reg_1); 
  bit_8_block_2_new <= bit_8_block_2_reg when (bit_8_block_0_zero_flag_reg='1') and 
				      (bit_8_block_1_zero_flag_reg='1') else 
		       exor(bit_8_block_2_reg, conv_sin_reg_1); 
  bit_8_block_3_new <= bit_8_block_3_reg when (bit_8_block_0_zero_flag_reg='1') and 
				      (bit_8_block_1_zero_flag_reg='1') and 
				      (bit_8_block_2_zero_flag_reg='1') else 
		       exor(bit_8_block_3_reg, conv_sin_reg_1); 


  bit_8_block_next <= bit_8_block_0_new when not (bit_8_block_0_zero_flag_reg='1') else
                  bit_8_block_1_new when not (bit_8_block_1_zero_flag_reg='1') else
                  bit_8_block_2_new when not (bit_8_block_2_zero_flag_reg='1') else
                  bit_8_block_3_new;


  conv_to_non_neg_1: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_8_block_0_new_reg_1 <= bit_8_block_0_new;
      bit_8_block_0_zero_flag_reg_2 <= bit_8_block_0_zero_flag_reg_1;
      bit_8_block_1_new_reg_1 <= bit_8_block_1_new;
      bit_8_block_1_zero_flag_reg_2 <= bit_8_block_1_zero_flag_reg_1;
      bit_8_block_2_new_reg_1 <= bit_8_block_2_new;
      bit_8_block_2_zero_flag_reg_2 <= bit_8_block_2_zero_flag_reg_1;
      bit_8_block_3_new_reg_1 <= bit_8_block_3_new;
      conv_sin_reg_2 <= conv_sin_reg_1;
      bit_8_block_next_reg <= bit_8_block_next;
      vec_int_tmp_1 <= vec_int_tmp_0;
    end if;
  end process conv_to_non_neg_1;


  bit_2_block_0 <= break_apart_0(bit_8_block_next_reg);
  bit_2_block_1 <= break_apart_1(bit_8_block_next_reg);
  bit_2_block_2 <= break_apart_2(bit_8_block_next_reg);
  bit_2_block_3 <= break_apart_3(bit_8_block_next_reg);


  bit_2_block_0_zero_flag <= not ( bit_2_block_0(0) or 
	  	 	 	   bit_2_block_0(1)); 
  bit_2_block_1_zero_flag <= not ( bit_2_block_1(0) or 
	  	 	 	   bit_2_block_1(1)); 
  bit_2_block_2_zero_flag <= not ( bit_2_block_2(0) or 
	  	 	 	   bit_2_block_2(1)); 


  conv_to_non_neg_2: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_2_block_0_zero_flag_reg <= bit_2_block_0_zero_flag;
      bit_2_block_0_reg <= bit_2_block_0;
      bit_2_block_1_zero_flag_reg <= bit_2_block_1_zero_flag;
      bit_2_block_1_reg <= bit_2_block_1;
      bit_2_block_2_zero_flag_reg <= bit_2_block_2_zero_flag;
      bit_2_block_2_reg <= bit_2_block_2;
      bit_2_block_3_reg <= bit_2_block_3;
      conv_sin_reg_3 <= conv_sin_reg_2;
      bit_2_block_0_zero_flag_reg_1 <= bit_2_block_0_zero_flag;
      bit_2_block_1_zero_flag_reg_1 <= bit_2_block_1_zero_flag;
      bit_2_block_2_zero_flag_reg_1 <= bit_2_block_2_zero_flag;
      bit_8_block_0_new_reg_2 <= bit_8_block_0_new_reg_1;
      bit_8_block_0_zero_flag_reg_3 <= bit_8_block_0_zero_flag_reg_2;
      bit_8_block_1_new_reg_2 <= bit_8_block_1_new_reg_1;
      bit_8_block_1_zero_flag_reg_3 <= bit_8_block_1_zero_flag_reg_2;
      bit_8_block_2_new_reg_2 <= bit_8_block_2_new_reg_1;
      bit_8_block_2_zero_flag_reg_3 <= bit_8_block_2_zero_flag_reg_2;
      bit_8_block_3_new_reg_2 <= bit_8_block_3_new_reg_1;
      vec_int_tmp_2 <= vec_int_tmp_1;
    end if;
  end process conv_to_non_neg_2;


  bit_2_block_0_new <= bit_2_block_0_reg;
  bit_2_block_1_new <= bit_2_block_1_reg when (bit_2_block_0_zero_flag_reg='1') else 
		       exor(bit_2_block_1_reg, conv_sin_reg_3); 
  bit_2_block_2_new <= bit_2_block_2_reg when (bit_2_block_0_zero_flag_reg='1') and 
				      (bit_2_block_1_zero_flag_reg='1') else 
		       exor(bit_2_block_2_reg, conv_sin_reg_3); 
  bit_2_block_3_new <= bit_2_block_3_reg when (bit_2_block_0_zero_flag_reg='1') and 
				      (bit_2_block_1_zero_flag_reg='1') and 
				      (bit_2_block_2_zero_flag_reg='1') else 
		       exor(bit_2_block_3_reg, conv_sin_reg_3); 


  bit_2_block_next <= bit_2_block_0_new when not (bit_2_block_0_zero_flag_reg='1') else
                  bit_2_block_1_new when not (bit_2_block_1_zero_flag_reg='1') else
                  bit_2_block_2_new when not (bit_2_block_2_zero_flag_reg='1') else
                  bit_2_block_3_new;


  conv_to_non_neg_3: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_2_block_0_new_reg_1 <= bit_2_block_0_new;
      bit_2_block_0_zero_flag_reg_2 <= bit_2_block_0_zero_flag_reg_1;
      bit_2_block_1_new_reg_1 <= bit_2_block_1_new;
      bit_2_block_1_zero_flag_reg_2 <= bit_2_block_1_zero_flag_reg_1;
      bit_2_block_2_new_reg_1 <= bit_2_block_2_new;
      bit_2_block_2_zero_flag_reg_2 <= bit_2_block_2_zero_flag_reg_1;
      bit_2_block_3_new_reg_1 <= bit_2_block_3_new;
      bit_8_block_0_new_reg_3 <= bit_8_block_0_new_reg_2;
      bit_8_block_0_zero_flag_reg_4 <= bit_8_block_0_zero_flag_reg_3;
      bit_8_block_1_new_reg_3 <= bit_8_block_1_new_reg_2;
      bit_8_block_1_zero_flag_reg_4 <= bit_8_block_1_zero_flag_reg_3;
      bit_8_block_2_new_reg_3 <= bit_8_block_2_new_reg_2;
      bit_8_block_2_zero_flag_reg_4 <= bit_8_block_2_zero_flag_reg_3;
      bit_8_block_3_new_reg_3 <= bit_8_block_3_new_reg_2;
      conv_sin_reg_4 <= conv_sin_reg_3;
      bit_2_block_next_reg <= bit_2_block_next;
      vec_int_tmp_3 <= vec_int_tmp_2;
    end if;
  end process conv_to_non_neg_3;


  bit_1_block_1 <= break_apart_t_1_bit(bit_2_block_next_reg);
  bit_1_block_0 <= break_apart_b_1_bit(bit_2_block_next_reg);


  bit_1_block_0_zero_flag <= not (bit_1_block_0);


  conv_to_non_neg_4: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_1_block_0_zero_flag_reg <= bit_1_block_0_zero_flag;
      bit_1_block_0_reg <= bit_1_block_0;
      bit_1_block_1_reg <= bit_1_block_1;
      conv_sin_reg_5 <= conv_sin_reg_4;
      bit_1_block_0_zero_flag_reg_1 <= bit_1_block_0_zero_flag;
      bit_2_block_0_new_reg_2 <= bit_2_block_0_new_reg_1;
      bit_2_block_0_zero_flag_reg_3 <= bit_2_block_0_zero_flag_reg_2;
      bit_2_block_1_new_reg_2 <= bit_2_block_1_new_reg_1;
      bit_2_block_1_zero_flag_reg_3 <= bit_2_block_1_zero_flag_reg_2;
      bit_2_block_2_new_reg_2 <= bit_2_block_2_new_reg_1;
      bit_2_block_2_zero_flag_reg_3 <= bit_2_block_2_zero_flag_reg_2;
      bit_2_block_3_new_reg_2 <= bit_2_block_3_new_reg_1;
      bit_8_block_0_new_reg_4 <= bit_8_block_0_new_reg_3;
      bit_8_block_0_zero_flag_reg_5 <= bit_8_block_0_zero_flag_reg_4;
      bit_8_block_1_new_reg_4 <= bit_8_block_1_new_reg_3;
      bit_8_block_1_zero_flag_reg_5 <= bit_8_block_1_zero_flag_reg_4;
      bit_8_block_2_new_reg_4 <= bit_8_block_2_new_reg_3;
      bit_8_block_2_zero_flag_reg_5 <= bit_8_block_2_zero_flag_reg_4;
      bit_8_block_3_new_reg_4 <= bit_8_block_3_new_reg_3;
      vec_int_tmp_4 <= vec_int_tmp_3;
    end if;
  end process conv_to_non_neg_4;


  bit_1_block_0_new <= bit_1_block_0_reg;
  bit_1_block_1_new <= bit_1_block_1_reg when (bit_1_block_0_zero_flag_reg='1') else 
		       (bit_1_block_1_reg and not conv_sin_reg_5) or (not bit_1_block_1_reg and conv_sin_reg_5); 


  bit_1_block_next <= bit_1_block_0_new when not (bit_1_block_0_zero_flag_reg='1') else
                  bit_1_block_1_new;


  conv_to_non_neg_5: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_1_block_0_new_reg_1 <= bit_1_block_0_new;
      bit_1_block_0_zero_flag_reg_2 <= bit_1_block_0_zero_flag_reg_1;
      bit_1_block_1_new_reg_1 <= bit_1_block_1_new;
      bit_2_block_0_new_reg_3 <= bit_2_block_0_new_reg_2;
      bit_2_block_0_zero_flag_reg_4 <= bit_2_block_0_zero_flag_reg_3;
      bit_2_block_1_new_reg_3 <= bit_2_block_1_new_reg_2;
      bit_2_block_1_zero_flag_reg_4 <= bit_2_block_1_zero_flag_reg_3;
      bit_2_block_2_new_reg_3 <= bit_2_block_2_new_reg_2;
      bit_2_block_2_zero_flag_reg_4 <= bit_2_block_2_zero_flag_reg_3;
      bit_2_block_3_new_reg_3 <= bit_2_block_3_new_reg_2;
      bit_8_block_0_new_reg_5 <= bit_8_block_0_new_reg_4;
      bit_8_block_0_zero_flag_reg_6 <= bit_8_block_0_zero_flag_reg_5;
      bit_8_block_1_new_reg_5 <= bit_8_block_1_new_reg_4;
      bit_8_block_1_zero_flag_reg_6 <= bit_8_block_1_zero_flag_reg_5;
      bit_8_block_2_new_reg_5 <= bit_8_block_2_new_reg_4;
      bit_8_block_2_zero_flag_reg_6 <= bit_8_block_2_zero_flag_reg_5;
      bit_8_block_3_new_reg_5 <= bit_8_block_3_new_reg_4;
      conv_sin_reg_6 <= conv_sin_reg_5;
      bit_1_block_next_reg <= bit_1_block_next;
      vec_int_tmp_5 <= vec_int_tmp_4;
    end if;
  end process conv_to_non_neg_5;


  bit_1_block_0_final <= bit_1_block_next_reg when not (bit_1_block_0_zero_flag_reg_2='1') else
                         bit_1_block_0_new_reg_1;
  bit_1_block_1_final <= bit_1_block_next_reg when (bit_1_block_0_zero_flag_reg_2='1') else
                         bit_1_block_1_new_reg_1;
  bit_1_block_0_final_0 <= bit_1_block_0_final;
  bit_1_block_0_final_1 <= bit_1_block_0_final;
  bit_1_block_0_final_2 <= bit_1_block_0_final;
  bit_1_block_0_final_3 <= bit_1_block_0_final;
  bit_1_block_1_final_0 <= bit_1_block_1_final;
  bit_1_block_1_final_1 <= bit_1_block_1_final;
  bit_1_block_1_final_2 <= bit_1_block_1_final;
  bit_1_block_1_final_3 <= bit_1_block_1_final;

  bit_2_block_0_final <= bit_1_block_1_final_0 & bit_1_block_0_final_0 when not (bit_2_block_0_zero_flag_reg_4='1') else
                         bit_2_block_0_new_reg_3;
  bit_2_block_1_final <= bit_1_block_1_final_1 & bit_1_block_0_final_1 when (bit_2_block_0_zero_flag_reg_4='1') and
                                              not (bit_2_block_1_zero_flag_reg_4='1') else
                         bit_2_block_1_new_reg_3;
  bit_2_block_2_final <= bit_1_block_1_final_2 & bit_1_block_0_final_2 when (bit_2_block_0_zero_flag_reg_4='1') and
                                              (bit_2_block_1_zero_flag_reg_4='1') and
                                              not (bit_2_block_2_zero_flag_reg_4='1') else
                         bit_2_block_2_new_reg_3;
  bit_2_block_3_final <= bit_1_block_1_final_3 & bit_1_block_0_final_3 when (bit_2_block_0_zero_flag_reg_4='1') and
                                              (bit_2_block_1_zero_flag_reg_4='1') and
                                              (bit_2_block_2_zero_flag_reg_4='1') else
                         bit_2_block_3_new_reg_3;
  
  bit_2_block_0_final_0 <= bit_2_block_0_final;
  bit_2_block_0_final_1 <= bit_2_block_0_final;
  bit_2_block_0_final_2 <= bit_2_block_0_final;
  bit_2_block_0_final_3 <= bit_2_block_0_final;
  bit_2_block_1_final_0 <= bit_2_block_1_final;
  bit_2_block_1_final_1 <= bit_2_block_1_final;
  bit_2_block_1_final_2 <= bit_2_block_1_final;
  bit_2_block_1_final_3 <= bit_2_block_1_final;
  bit_2_block_2_final_0 <= bit_2_block_2_final;
  bit_2_block_2_final_1 <= bit_2_block_2_final;
  bit_2_block_2_final_2 <= bit_2_block_2_final;
  bit_2_block_2_final_3 <= bit_2_block_2_final;
  bit_2_block_3_final_0 <= bit_2_block_3_final;
  bit_2_block_3_final_1 <= bit_2_block_3_final;
  bit_2_block_3_final_2 <= bit_2_block_3_final;
  bit_2_block_3_final_3 <= bit_2_block_3_final;
  
  bit_8_block_0_final <= bit_2_block_3_final_0 & bit_2_block_2_final_0 & bit_2_block_1_final_0 & bit_2_block_0_final_0 when not (bit_8_block_0_zero_flag_reg_6='1') else
                         bit_8_block_0_new_reg_5;
  bit_8_block_1_final <= bit_2_block_3_final_1 & bit_2_block_2_final_1 & bit_2_block_1_final_1 & bit_2_block_0_final_1 when (bit_8_block_0_zero_flag_reg_6='1') and
                                              not (bit_8_block_1_zero_flag_reg_6='1') else
                         bit_8_block_1_new_reg_5;
  bit_8_block_2_final <= bit_2_block_3_final_2 & bit_2_block_2_final_2 & bit_2_block_1_final_2 & bit_2_block_0_final_2 when (bit_8_block_0_zero_flag_reg_6='1') and
                                              (bit_8_block_1_zero_flag_reg_6='1') and
                                              not (bit_8_block_2_zero_flag_reg_6='1') else
                         bit_8_block_2_new_reg_5;
  bit_8_block_3_final <= bit_2_block_3_final_3 & bit_2_block_2_final_3 & bit_2_block_1_final_3 & bit_2_block_0_final_3 when (bit_8_block_0_zero_flag_reg_6='1') and
                                              (bit_8_block_1_zero_flag_reg_6='1') and
                                              (bit_8_block_2_zero_flag_reg_6='1') else
                         bit_8_block_3_new_reg_5;


  bit_32_block_0_final <= bit_8_block_3_final & bit_8_block_2_final & bit_8_block_1_final & bit_8_block_0_final;

  save_final_result: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      if(conv_sin_reg_6 =  '1') then
        vec_int(30 downto 0) <= bit_32_block_0_final(30 downto 0);
        vec_int(31) <= conv_sin_reg_6;
      else
        vec_int(30 downto 0) <= vec_int_tmp_5(30 downto 0);
        vec_int(31) <= conv_sin_reg_6;
      end if;
    end if;
  end process save_final_result;

    
end architecture behavioural;
