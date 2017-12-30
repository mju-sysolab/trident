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
--I only got this up to 153.11591MHz

library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity mux64_dptoi is
  generic (constant pinmap : in integer);
  port(output: out std_logic;
       clk: in std_logic;
       sel: in std_logic_vector(5 downto 0);
       inputs: in std_logic_vector(63 downto 0));
end entity mux64_dptoi;

architecture mux64_arch of mux64_dptoi is
    component mux4 is
      port(output: out std_logic;
	   s0: in std_logic;
	   s1: in std_logic;
	   in0: in std_logic;
	   in1: in std_logic;
	   in2: in std_logic;
	   in3: in std_logic);
    end component mux4;
    signal inputbuffer: std_logic_vector(63 downto 0);
    signal inputbuffer_0: std_logic_vector(63 downto 0);
    signal mux4_0_out: std_logic_vector(0 to 15);
    signal mux4_0_out_reg: std_logic_vector(0 to 15);
    signal mux4_1_out: std_logic_vector(0 to 3);
    signal mux4_1_out_reg: std_logic_vector(0 to 3);
    signal sel_0: std_logic_vector(5 downto 0);
    signal sel_in: std_logic_vector(5 downto 0);
    signal sel_0_0: std_logic_vector(5 downto 0);
    signal sel_0_1: std_logic_vector(5 downto 0);
    signal sel_0_2: std_logic_vector(5 downto 0);
    signal sel_0_3: std_logic_vector(5 downto 0);
    signal sel_0_4: std_logic_vector(5 downto 0);
    signal sel_0_5: std_logic_vector(5 downto 0);
    signal sel_0_6: std_logic_vector(5 downto 0);
    signal sel_0_7: std_logic_vector(5 downto 0);
    signal sel_1: std_logic_vector(5 downto 0);
    signal sel_2: std_logic_vector(5 downto 0);
    constant pin0 : integer := 63-pinmap;
begin
  input_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
    inputbuffer_0(63 downto pin0) <= inputs(pinmap downto 0);
    inputbuffer_0(pin0 - 1 downto 0) <= (others => '0');
    sel_in <= sel;
    end if;
  end process input_reg;
  split_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
    inputbuffer <= inputbuffer_0;
    sel_0 <= sel_in;
    sel_0_0 <= sel_in;
    sel_0_1 <= sel_in;
    sel_0_2 <= sel_in;
    sel_0_3 <= sel_in;
    sel_0_4 <= sel_in;
    sel_0_5 <= sel_in;
    sel_0_6 <= sel_in;
    sel_0_7 <= sel_in;
    end if;
  end process split_reg;


  mux4_0 : component mux4
    port map(
      output => mux4_0_out(0),
      s0 => sel_0_0(0),
      s1 => sel_0_0(1),
      in0 => inputbuffer(63),
      in1 => inputbuffer(62),
      in2 => inputbuffer(61),
      in3 => inputbuffer(60)
    );
  mux4_1 : component mux4
    port map(
      output => mux4_0_out(1),
      s0 => sel_0_0(0),
      s1 => sel_0_0(1),
      in0 => inputbuffer(59),
      in1 => inputbuffer(58),
      in2 => inputbuffer(57),
      in3 => inputbuffer(56)
    );
  mux4_2 : component mux4
    port map(
      output => mux4_0_out(2),
      s0 => sel_0_1(0),
      s1 => sel_0_1(1),
      in0 => inputbuffer(55),
      in1 => inputbuffer(54),
      in2 => inputbuffer(53),
      in3 => inputbuffer(52)
    );
  mux4_3 : component mux4
    port map(
      output => mux4_0_out(3),
      s0 => sel_0_1(0),
      s1 => sel_0_1(1),
      in0 => inputbuffer(51),
      in1 => inputbuffer(50),
      in2 => inputbuffer(49),
      in3 => inputbuffer(48)
    );
  mux4_4 : component mux4
    port map(
      output => mux4_0_out(4),
      s0 => sel_0_2(0),
      s1 => sel_0_2(1),
      in0 => inputbuffer(47),
      in1 => inputbuffer(46),
      in2 => inputbuffer(45),
      in3 => inputbuffer(44)
    );
  mux4_5 : component mux4
    port map(
      output => mux4_0_out(5),
      s0 => sel_0_2(0),
      s1 => sel_0_2(1),
      in0 => inputbuffer(43),
      in1 => inputbuffer(42),
      in2 => inputbuffer(41),
      in3 => inputbuffer(40)
    );
  mux4_6 : component mux4
    port map(
      output => mux4_0_out(6),
      s0 => sel_0_3(0),
      s1 => sel_0_3(1),
      in0 => inputbuffer(39),
      in1 => inputbuffer(38),
      in2 => inputbuffer(37),
      in3 => inputbuffer(36)
    );
  mux4_7 : component mux4
    port map(
      output => mux4_0_out(7),
      s0 => sel_0_3(0),
      s1 => sel_0_3(1),
      in0 => inputbuffer(35),
      in1 => inputbuffer(34),
      in2 => inputbuffer(33),
      in3 => inputbuffer(32)
    );
  mux4_8 : component mux4
    port map(
      output => mux4_0_out(8),
      s0 => sel_0_4(0),
      s1 => sel_0_4(1),
      in0 => inputbuffer(31),
      in1 => inputbuffer(30),
      in2 => inputbuffer(29),
      in3 => inputbuffer(28)
    );
  mux4_9 : component mux4
    port map(
      output => mux4_0_out(9),
      s0 => sel_0_4(0),
      s1 => sel_0_4(1),
      in0 => inputbuffer(27),
      in1 => inputbuffer(26),
      in2 => inputbuffer(25),
      in3 => inputbuffer(24)
    );
  mux4_10 : component mux4
    port map(
      output => mux4_0_out(10),
      s0 => sel_0_5(0),
      s1 => sel_0_5(1),
      in0 => inputbuffer(23),
      in1 => inputbuffer(22),
      in2 => inputbuffer(21),
      in3 => inputbuffer(20)
    );
  mux4_11 : component mux4
    port map(
      output => mux4_0_out(11),
      s0 => sel_0_5(0),
      s1 => sel_0_5(1),
      in0 => inputbuffer(19),
      in1 => inputbuffer(18),
      in2 => inputbuffer(17),
      in3 => inputbuffer(16)
    );
  mux4_12 : component mux4
    port map(
      output => mux4_0_out(12),
      s0 => sel_0_6(0),
      s1 => sel_0_6(1),
      in0 => inputbuffer(15),
      in1 => inputbuffer(14),
      in2 => inputbuffer(13),
      in3 => inputbuffer(12)
    );
  mux4_13 : component mux4
    port map(
      output => mux4_0_out(13),
      s0 => sel_0_6(0),
      s1 => sel_0_6(1),
      in0 => inputbuffer(11),
      in1 => inputbuffer(10),
      in2 => inputbuffer(9),
      in3 => inputbuffer(8)
    );
  mux4_14 : component mux4
    port map(
      output => mux4_0_out(14),
      s0 => sel_0_7(0),
      s1 => sel_0_7(1),
      in0 => inputbuffer(7),
      in1 => inputbuffer(6),
      in2 => inputbuffer(5),
      in3 => inputbuffer(4)
    );
  mux4_15 : component mux4
    port map(
      output => mux4_0_out(15),
      s0 => sel_0_7(0),
      s1 => sel_0_7(1),
      in0 => inputbuffer(3),
      in1 => inputbuffer(2),
      in2 => inputbuffer(1),
      in3 => inputbuffer(0)
    );

  middle_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
      mux4_0_out_reg <= mux4_0_out;
      sel_1 <= sel_0;
    end if;
  end process middle_reg;

  mux4_16 : component mux4
    port map(
      output => mux4_1_out(0),
      s0 => sel_1(2),
      s1 => sel_1(3),
      in0 => mux4_0_out_reg(0),
      in1 => mux4_0_out_reg(1),
      in2 => mux4_0_out_reg(2),
      in3 => mux4_0_out_reg(3)
    );
  mux4_17 : component mux4
    port map(
      output => mux4_1_out(1),
      s0 => sel_1(2),
      s1 => sel_1(3),
      in0 => mux4_0_out_reg(4),
      in1 => mux4_0_out_reg(5),
      in2 => mux4_0_out_reg(6),
      in3 => mux4_0_out_reg(7)
    );
  mux4_18 : component mux4
    port map(
      output => mux4_1_out(2),
      s0 => sel_1(2),
      s1 => sel_1(3),
      in0 => mux4_0_out_reg(8),
      in1 => mux4_0_out_reg(9),
      in2 => mux4_0_out_reg(10),
      in3 => mux4_0_out_reg(11)
    );
  mux4_19 : component mux4
    port map(
      output => mux4_1_out(3),
      s0 => sel_1(2),
      s1 => sel_1(3),
      in0 => mux4_0_out_reg(12),
      in1 => mux4_0_out_reg(13),
      in2 => mux4_0_out_reg(14),
      in3 => mux4_0_out_reg(15)
    );

  last_reg: process (clk)
  begin
    if (clk'event and clk = '1')  then
      mux4_1_out_reg <= mux4_1_out;
      sel_2 <= sel_1;
    end if;
  end process last_reg;


  mux4_20 : component mux4
    port map(
      output => output,
      s0 => sel_2(4),
      s1 => sel_2(5),
      in0 => mux4_1_out_reg(0),
      in1 => mux4_1_out_reg(1),
      in2 => mux4_1_out_reg(2),
      in3 => mux4_1_out_reg(3)
    );
  
end architecture mux64_arch;

library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity log_shift_left_dptoi is
  port(data_in: in std_logic_vector(63 downto 0);
       shift_cnt: in std_logic_vector(5 downto 0);
       data_out: out std_logic_vector(63 downto 0);
       clk: in std_logic);
end entity log_shift_left_dptoi;

architecture log_shift_left_arch of log_shift_left_dptoi is
   component mux64_dptoi is
     generic (constant pinmap : in integer);
     port(output: out std_logic;
          clk: in std_logic;
          sel: in std_logic_vector(5 downto 0);
          inputs: in std_logic_vector(63 downto 0));
   end component mux64_dptoi;
   signal out_buffer: std_logic_vector(63 downto 0);
   signal input_buffer: std_logic_vector(63 downto 0);
   signal input_buffer_0: std_logic_vector(63 downto 0);
   signal input_buffer_reg_0: std_logic_vector(63 downto 0);
   signal input_buffer_0_wire: std_logic_vector(63 downto 0);
   signal input_buffer_1: std_logic_vector(63 downto 0);
   signal input_buffer_reg_1: std_logic_vector(63 downto 0);
   signal input_buffer_1_wire: std_logic_vector(63 downto 0);
   signal input_buffer_2: std_logic_vector(63 downto 0);
   signal input_buffer_2_wire: std_logic_vector(63 downto 0);
   signal input_buffer_3: std_logic_vector(63 downto 0);
   signal input_buffer_3_wire: std_logic_vector(63 downto 0);
   signal input_buffer_4: std_logic_vector(63 downto 0);
   signal input_buffer_4_wire: std_logic_vector(63 downto 0);
   signal input_buffer_5: std_logic_vector(63 downto 0);
   signal input_buffer_5_wire: std_logic_vector(63 downto 0);
   signal input_buffer_6: std_logic_vector(63 downto 0);
   signal input_buffer_6_wire: std_logic_vector(63 downto 0);
   signal input_buffer_7: std_logic_vector(63 downto 0);
   signal input_buffer_7_wire: std_logic_vector(63 downto 0);
   signal shift_cnt_reg: std_logic_vector(5 downto 0);
   signal shift_cnt_reg_0: std_logic_vector(5 downto 0);
   signal shift_cnt_reg_1: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_0: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_1: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_2: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_3: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_4: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_5: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_6: std_logic_vector(5 downto 0);
   signal shift_cnt_wire_7: std_logic_vector(5 downto 0);
   signal shift_cnt_0: std_logic_vector(5 downto 0);
   signal shift_cnt_1: std_logic_vector(5 downto 0);
   signal shift_cnt_2: std_logic_vector(5 downto 0);
   signal shift_cnt_3: std_logic_vector(5 downto 0);
   signal shift_cnt_4: std_logic_vector(5 downto 0);
   signal shift_cnt_5: std_logic_vector(5 downto 0);
   signal shift_cnt_6: std_logic_vector(5 downto 0);
   signal shift_cnt_7: std_logic_vector(5 downto 0);
begin
  divide_data: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
	input_buffer <= data_in;
	shift_cnt_reg <= shift_cnt;
      end if;
  end process divide_data;
  divide_data_0: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
	input_buffer_reg_0 <= input_buffer;
	input_buffer_reg_1 <= input_buffer;
	shift_cnt_reg_0 <= shift_cnt_reg;
	shift_cnt_reg_1 <= shift_cnt_reg;
      end if;
  end process divide_data_0;
  input_buffer_0_wire <= input_buffer_reg_0;
  input_buffer_1_wire <= input_buffer_reg_0;
  input_buffer_2_wire <= input_buffer_reg_0;
  input_buffer_3_wire <= input_buffer_reg_0;
  input_buffer_4_wire <= input_buffer_reg_1;
  input_buffer_5_wire <= input_buffer_reg_1;
  input_buffer_6_wire <= input_buffer_reg_1;
  input_buffer_7_wire <= input_buffer_reg_1;
  shift_cnt_wire_0 <= shift_cnt_reg_0;
  shift_cnt_wire_1 <= shift_cnt_reg_0;
  shift_cnt_wire_2 <= shift_cnt_reg_0;
  shift_cnt_wire_3 <= shift_cnt_reg_0;
  shift_cnt_wire_4 <= shift_cnt_reg_1;
  shift_cnt_wire_5 <= shift_cnt_reg_1;
  shift_cnt_wire_6 <= shift_cnt_reg_1;
  shift_cnt_wire_7 <= shift_cnt_reg_1;
  divide_data_2: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
	input_buffer_0 <= input_buffer_0_wire;
	input_buffer_1 <= input_buffer_1_wire;
	input_buffer_2 <= input_buffer_2_wire;
	input_buffer_3 <= input_buffer_3_wire;
	input_buffer_4 <= input_buffer_4_wire;
	input_buffer_5 <= input_buffer_5_wire;
	input_buffer_6 <= input_buffer_6_wire;
	input_buffer_7 <= input_buffer_7_wire;
	shift_cnt_0 <= shift_cnt_wire_0;
	shift_cnt_1 <= shift_cnt_wire_1;
	shift_cnt_2 <= shift_cnt_wire_2;
	shift_cnt_3 <= shift_cnt_wire_3;
	shift_cnt_4 <= shift_cnt_wire_4;
	shift_cnt_5 <= shift_cnt_wire_5;
	shift_cnt_6 <= shift_cnt_wire_6;
	shift_cnt_7 <= shift_cnt_wire_7;
      end if;
  end process divide_data_2;
  --foreachpin: for pin0 in 0 to data_in'high generate
  --begin
  --  mux64_shift: component mux64
  --  generic map (pinmap => pin0)
  --  port map ( output => out_buffer(pin0),
  --             clk => clk,
  --  	       inputs(63 downto 0) => 
  --  		    data_in(63 downto 0),
  --  	       sel => shift_cnt);
  --end generate foreachpin;
  
  foreachpin_0: for pin0 in 0 to 7 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_0(63 downto 0),
	             sel => shift_cnt_0);
  end generate foreachpin_0;
  foreachpin_1: for pin0 in 8 to 15 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_1(63 downto 0),
	             sel => shift_cnt_1);
  end generate foreachpin_1;
  foreachpin_2: for pin0 in 16 to 23 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_2(63 downto 0),
	             sel => shift_cnt_2);
  end generate foreachpin_2;
  foreachpin_3: for pin0 in 24 to 31 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_3(63 downto 0),
	             sel => shift_cnt_3);
  end generate foreachpin_3;
  foreachpin_4: for pin0 in 32 to 39 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_4(63 downto 0),
	             sel => shift_cnt_4);
  end generate foreachpin_4;
  foreachpin_5: for pin0 in 40 to 47 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_5(63 downto 0),
	             sel => shift_cnt_5);
  end generate foreachpin_5;
  foreachpin_6: for pin0 in 48 to 55 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_6(63 downto 0),
	             sel => shift_cnt_6);
  end generate foreachpin_6;
  foreachpin_7: for pin0 in 56 to 63 generate
  begin
	mux64_shift: component mux64_dptoi
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_7(63 downto 0),
	             sel => shift_cnt_7);
  end generate foreachpin_7;
  
  
  store_data: process (clk) is 
  begin
      if  (clk'event and clk = '1')  then
        data_out <= out_buffer;
      end if;
  end process store_data;
  
end architecture log_shift_left_arch;

library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity dp_to_i is
  port(vec_int: out std_logic_vector(63 downto 0);
       vec_dp: in std_logic_vector(63 downto 0);
       clk: in std_logic);
end entity dp_to_i;

architecture behavioural of dp_to_i is

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

    component log_shift_left_dptoi is
      port(data_in: in std_logic_vector(63 downto 0);
           shift_cnt: in std_logic_vector(5 downto 0);
           data_out: out std_logic_vector(63 downto 0);
	   clk: in std_logic);
    end component log_shift_left_dptoi;

    signal mant: std_logic_vector(63 downto 0);
    signal mant1: std_logic_vector(63 downto 0);
    --signal mant_tmp: std_logic_vector(30 downto 0);
    --signal mant_tmp2: std_logic_vector(30 downto 0);
    signal sine: std_logic;
    signal sine_save0: std_logic;
    signal sine_save1: std_logic;
    signal sine_save2: std_logic;
    signal sine_save3: std_logic;
    signal sine_save3_0: std_logic;
    signal sine_save3_1: std_logic;
    signal sine_save3_2: std_logic;
    signal sine_save4: std_logic;
    signal sine_save5: std_logic;
    signal sine_save6: std_logic;
    signal sine_save7: std_logic;
    signal exp: std_logic_vector(10 downto 0);
    signal exp_save0: std_logic_vector(10 downto 0);
    signal exp_save0_0: std_logic_vector(10 downto 0);
    signal exp_save0_0_plus_one: std_logic_vector(10 downto 0);
    signal exp_save0_0_0: std_logic;
    signal exp_save0_0_1: std_logic_vector(1 downto 0);
    signal exp_save0_0_2: std_logic_vector(2 downto 0);
    signal exp_save0_0_3: std_logic_vector(3 downto 0);
    signal exp_save0_0_4: std_logic_vector(4 downto 0);
    signal exp_save0_0_5: std_logic_vector(5 downto 0);
    signal exp_save0_0_6: std_logic_vector(6 downto 0);
    signal exp_save0_0_7: std_logic_vector(7 downto 0);
    signal exp_save0_0_8: std_logic_vector(8 downto 0);
    signal exp_save0_0_9: std_logic_vector(9 downto 0);
    signal exp_save0_1: std_logic_vector(10 downto 0);
    signal exp_save0_2: std_logic_vector(10 downto 0);
    signal exp_save1: std_logic_vector(10 downto 0);
    signal exp_save2: std_logic_vector(10 downto 0);
    signal exp_save3: std_logic_vector(10 downto 0);
    signal exp_save3_0: std_logic_vector(10 downto 0);
    signal exp_save3_1: std_logic_vector(10 downto 0);
    signal exp_save3_2: std_logic_vector(10 downto 0);
    signal exp_save4: std_logic_vector(10 downto 0);
    signal exp_save4_0: std_logic_vector(10 downto 0);
    signal exp_save4_1: std_logic_vector(10 downto 0);
    signal exp_save4_2: std_logic_vector(10 downto 0);
    signal exp_save4_3: std_logic_vector(10 downto 0);
    --signal exp_save5: std_logic_vector(7 downto 0);
    signal exp_save5_0: std_logic_vector(10 downto 0);
    signal exp_save5_1: std_logic_vector(10 downto 0);
    signal exp_save5_2: std_logic_vector(10 downto 0);
    signal exp_save5_3: std_logic_vector(10 downto 0);
    signal exp_save5_4: std_logic_vector(10 downto 0);
    signal exp_save5_5: std_logic_vector(10 downto 0);
    signal exp_save5_6: std_logic_vector(10 downto 0);
    signal exp_save5_7: std_logic_vector(10 downto 0);
    signal exp_save5_8: std_logic_vector(10 downto 0);
    signal exp_save5_9: std_logic_vector(10 downto 0);
    signal exp_save5_10: std_logic_vector(10 downto 0);
    signal exp_save5_11: std_logic_vector(10 downto 0);
    signal exp_save5_12: std_logic_vector(10 downto 0);
    signal exp_save5_13: std_logic_vector(10 downto 0);
    signal exp_save5_14: std_logic_vector(10 downto 0);
    signal exp_save5_15: std_logic_vector(10 downto 0);
    --signal exp_save6: std_logic_vector(7 downto 0);
    --signal exp_save7: std_logic_vector(7 downto 0);
    --signal exp_save8: std_logic_vector(7 downto 0);
    --signal exp_save9: std_logic_vector(7 downto 0);
    --signal exp_save10: std_logic_vector(7 downto 0);
   -- signal exp_save11: std_logic_vector(7 downto 0);
    signal int_tmp: std_logic_vector(63 downto 0);
    signal int_tmp_0: std_logic_vector(63 downto 0);
    signal int_tmp_inv: std_logic_vector(63 downto 0);
    signal int_tmp_inv_0: std_logic_vector(63 downto 0);
    --signal int_tmp2: std_logic_vector(31 downto 0);
    --signal int_tmp3: std_logic_vector(30 downto 0);
    --signal int_tmp4: std_logic_vector(31 downto 0);
    signal exp_to_shift: std_logic_vector(10 downto 0);

    --2's compliment signals:
    signal bit_16_block_0: std_logic_vector(15 downto 0);
    signal bit_16_block_0_final: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_2: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_3: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_4: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_5: std_logic_vector(15 downto 0);
    signal bit_16_block_0_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_0_zero_flag: std_logic;
    signal bit_16_block_0_zero_flag_reg: std_logic;
    signal bit_16_block_0_zero_flag_reg_1: std_logic;
    signal bit_16_block_0_zero_flag_reg_2: std_logic;
    signal bit_16_block_0_zero_flag_reg_3: std_logic;
    signal bit_16_block_0_zero_flag_reg_4: std_logic;
    signal bit_16_block_0_zero_flag_reg_5: std_logic;
    signal bit_16_block_0_zero_flag_reg_6: std_logic;
    signal bit_16_block_1: std_logic_vector(15 downto 0);
    signal bit_16_block_1_final: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_2: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_3: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_4: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_5: std_logic_vector(15 downto 0);
    signal bit_16_block_1_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_1_zero_flag: std_logic;
    signal bit_16_block_1_zero_flag_reg: std_logic;
    signal bit_16_block_1_zero_flag_reg_1: std_logic;
    signal bit_16_block_1_zero_flag_reg_2: std_logic;
    signal bit_16_block_1_zero_flag_reg_3: std_logic;
    signal bit_16_block_1_zero_flag_reg_4: std_logic;
    signal bit_16_block_1_zero_flag_reg_5: std_logic;
    signal bit_16_block_1_zero_flag_reg_6: std_logic;
    signal bit_16_block_2: std_logic_vector(15 downto 0);
    signal bit_16_block_2_final: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new_reg_2: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new_reg_3: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new_reg_4: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new_reg_5: std_logic_vector(15 downto 0);
    signal bit_16_block_2_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_2_zero_flag: std_logic;
    signal bit_16_block_2_zero_flag_reg: std_logic;
    signal bit_16_block_2_zero_flag_reg_1: std_logic;
    signal bit_16_block_2_zero_flag_reg_2: std_logic;
    signal bit_16_block_2_zero_flag_reg_3: std_logic;
    signal bit_16_block_2_zero_flag_reg_4: std_logic;
    signal bit_16_block_2_zero_flag_reg_5: std_logic;
    signal bit_16_block_2_zero_flag_reg_6: std_logic;
    signal bit_16_block_3: std_logic_vector(15 downto 0);
    signal bit_16_block_3_final: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new_reg_2: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new_reg_3: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new_reg_4: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new_reg_5: std_logic_vector(15 downto 0);
    signal bit_16_block_3_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_next: std_logic_vector(15 downto 0);
    signal bit_16_block_next_reg: std_logic_vector(15 downto 0);
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
    signal bit_1_block_1_zero_flag: std_logic;
    signal bit_1_block_1_zero_flag_reg: std_logic;
    signal bit_1_block_1_zero_flag_reg_1: std_logic;
    signal bit_1_block_1_zero_flag_reg_2: std_logic;
    signal bit_1_block_2: std_logic;
    signal bit_1_block_2_final: std_logic;
    signal bit_1_block_2_new: std_logic;
    signal bit_1_block_2_new_reg_1: std_logic;
    signal bit_1_block_2_reg: std_logic;
    signal bit_1_block_2_zero_flag: std_logic;
    signal bit_1_block_2_zero_flag_reg: std_logic;
    signal bit_1_block_2_zero_flag_reg_1: std_logic;
    signal bit_1_block_2_zero_flag_reg_2: std_logic;
    signal bit_1_block_3: std_logic;
    signal bit_1_block_3_final: std_logic;
    signal bit_1_block_3_new: std_logic;
    signal bit_1_block_3_new_reg_1: std_logic;
    signal bit_1_block_3_reg: std_logic;
    signal bit_1_block_next: std_logic;
    signal bit_1_block_next_reg: std_logic;
    signal bit_4_block_0: std_logic_vector(3 downto 0);
    signal bit_4_block_0_final: std_logic_vector(3 downto 0);
    signal bit_4_block_0_new: std_logic_vector(3 downto 0);
    signal bit_4_block_0_new_reg_1: std_logic_vector(3 downto 0);
    signal bit_4_block_0_new_reg_2: std_logic_vector(3 downto 0);
    signal bit_4_block_0_new_reg_3: std_logic_vector(3 downto 0);
    signal bit_4_block_0_reg: std_logic_vector(3 downto 0);
    signal bit_4_block_0_zero_flag: std_logic;
    signal bit_4_block_0_zero_flag_reg: std_logic;
    signal bit_4_block_0_zero_flag_reg_1: std_logic;
    signal bit_4_block_0_zero_flag_reg_2: std_logic;
    signal bit_4_block_0_zero_flag_reg_3: std_logic;
    signal bit_4_block_0_zero_flag_reg_4: std_logic;
    signal bit_4_block_1: std_logic_vector(3 downto 0);
    signal bit_4_block_1_final: std_logic_vector(3 downto 0);
    signal bit_4_block_1_new: std_logic_vector(3 downto 0);
    signal bit_4_block_1_new_reg_1: std_logic_vector(3 downto 0);
    signal bit_4_block_1_new_reg_2: std_logic_vector(3 downto 0);
    signal bit_4_block_1_new_reg_3: std_logic_vector(3 downto 0);
    signal bit_4_block_1_reg: std_logic_vector(3 downto 0);
    signal bit_4_block_1_zero_flag: std_logic;
    signal bit_4_block_1_zero_flag_reg: std_logic;
    signal bit_4_block_1_zero_flag_reg_1: std_logic;
    signal bit_4_block_1_zero_flag_reg_2: std_logic;
    signal bit_4_block_1_zero_flag_reg_3: std_logic;
    signal bit_4_block_1_zero_flag_reg_4: std_logic;
    signal bit_4_block_2: std_logic_vector(3 downto 0);
    signal bit_4_block_2_final: std_logic_vector(3 downto 0);
    signal bit_4_block_2_new: std_logic_vector(3 downto 0);
    signal bit_4_block_2_new_reg_1: std_logic_vector(3 downto 0);
    signal bit_4_block_2_new_reg_2: std_logic_vector(3 downto 0);
    signal bit_4_block_2_new_reg_3: std_logic_vector(3 downto 0);
    signal bit_4_block_2_reg: std_logic_vector(3 downto 0);
    signal bit_4_block_2_zero_flag: std_logic;
    signal bit_4_block_2_zero_flag_reg: std_logic;
    signal bit_4_block_2_zero_flag_reg_1: std_logic;
    signal bit_4_block_2_zero_flag_reg_2: std_logic;
    signal bit_4_block_2_zero_flag_reg_3: std_logic;
    signal bit_4_block_2_zero_flag_reg_4: std_logic;
    signal bit_4_block_3: std_logic_vector(3 downto 0);
    signal bit_4_block_3_final: std_logic_vector(3 downto 0);
    signal bit_4_block_3_new: std_logic_vector(3 downto 0);
    signal bit_4_block_3_new_reg_1: std_logic_vector(3 downto 0);
    signal bit_4_block_3_new_reg_2: std_logic_vector(3 downto 0);
    signal bit_4_block_3_new_reg_3: std_logic_vector(3 downto 0);
    signal bit_4_block_3_reg: std_logic_vector(3 downto 0);
    signal bit_4_block_next: std_logic_vector(3 downto 0);
    signal bit_4_block_next_reg: std_logic_vector(3 downto 0);
    signal bit_64_block_0_final: std_logic_vector(63 downto 0);
    signal bit_64_block_next_reg: std_logic_vector(63 downto 0);
    signal conv_sin_reg_0: std_logic;
    signal conv_sin_reg_1: std_logic;
    signal conv_sin_reg_2: std_logic;
    signal conv_sin_reg_3: std_logic;
    signal conv_sin_reg_4: std_logic;
    signal conv_sin_reg_5: std_logic;
    signal conv_sin_reg_6: std_logic;
    signal vec_int_tmp: std_logic_vector(63 downto 0);
    signal vec_int_tmp_final: std_logic_vector(63 downto 0);
    signal vec_int_tmp_0: std_logic_vector(63 downto 0);
    signal vec_int_tmp_1: std_logic_vector(63 downto 0);
    signal vec_int_tmp_2: std_logic_vector(63 downto 0);
    signal vec_int_tmp_3: std_logic_vector(63 downto 0);
    signal vec_int_tmp_4: std_logic_vector(63 downto 0);
    signal vec_int_tmp_5: std_logic_vector(63 downto 0);
    signal bit_1_block_0_final_0: std_logic;
    signal bit_1_block_0_final_1: std_logic;
    signal bit_1_block_0_final_2: std_logic;
    signal bit_1_block_0_final_3: std_logic;
    signal bit_1_block_1_final_0: std_logic;
    signal bit_1_block_1_final_1: std_logic;
    signal bit_1_block_1_final_2: std_logic;
    signal bit_1_block_1_final_3: std_logic;
    signal bit_1_block_2_final_0: std_logic;
    signal bit_1_block_2_final_1: std_logic;
    signal bit_1_block_2_final_2: std_logic;
    signal bit_1_block_2_final_3: std_logic;
    signal bit_1_block_3_final_0: std_logic;
    signal bit_1_block_3_final_1: std_logic;
    signal bit_1_block_3_final_2: std_logic;
    signal bit_1_block_3_final_3: std_logic;
    signal bit_1_block_next_reg_0: std_logic;
    signal bit_1_block_next_reg_1: std_logic;
    signal bit_1_block_next_reg_2: std_logic;
    signal bit_1_block_next_reg_3: std_logic;
    signal bit_4_block_0_final_0: std_logic_vector(3 downto 0);
    signal bit_4_block_0_final_1: std_logic_vector(3 downto 0);
    signal bit_4_block_0_final_2: std_logic_vector(3 downto 0);
    signal bit_4_block_0_final_3: std_logic_vector(3 downto 0);
    signal bit_4_block_1_final_0: std_logic_vector(3 downto 0);
    signal bit_4_block_1_final_1: std_logic_vector(3 downto 0);
    signal bit_4_block_1_final_2: std_logic_vector(3 downto 0);
    signal bit_4_block_1_final_3: std_logic_vector(3 downto 0);
    signal bit_4_block_2_final_0: std_logic_vector(3 downto 0);
    signal bit_4_block_2_final_1: std_logic_vector(3 downto 0);
    signal bit_4_block_2_final_2: std_logic_vector(3 downto 0);
    signal bit_4_block_2_final_3: std_logic_vector(3 downto 0);
    signal bit_4_block_3_final_0: std_logic_vector(3 downto 0);
    signal bit_4_block_3_final_1: std_logic_vector(3 downto 0);
    signal bit_4_block_3_final_2: std_logic_vector(3 downto 0);
    signal bit_4_block_3_final_3: std_logic_vector(3 downto 0);
    signal bit_16_block_0_zero_flag_reg_6_0: std_logic;
    signal bit_16_block_0_zero_flag_reg_6_1: std_logic;
    signal bit_16_block_0_zero_flag_reg_6_2: std_logic;
    signal bit_16_block_0_zero_flag_reg_6_3: std_logic;
    signal bit_16_block_1_zero_flag_reg_6_0: std_logic;
    signal bit_16_block_1_zero_flag_reg_6_1: std_logic;
    signal bit_16_block_1_zero_flag_reg_6_2: std_logic;
    signal bit_16_block_2_zero_flag_reg_6_0: std_logic;
    signal bit_16_block_2_zero_flag_reg_6_1: std_logic;
begin
  
  sine <= vec_dp(63);
  exp <= vec_dp(62 downto 52) - b"110100";
  mant <= x"001" & vec_dp(51 downto 0) when exp(10) = '1' else
          invert_bits(x"001" & vec_dp(51 downto 0));
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
      exp_save0_0_7 <= exp(7 downto 0);
      exp_save0_0_8 <= exp(8 downto 0);
      exp_save0_0_9 <= exp(9 downto 0);
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
  exp_save0_0_plus_one(8) <= not exp_save0_0(8) when exp_save0_0_7 = b"11111111" else
                             exp_save0_0(8);
  exp_save0_0_plus_one(9) <= not exp_save0_0(9) when exp_save0_0_8 = b"111111111" else
                             exp_save0_0(9);
  exp_save0_0_plus_one(10) <= not exp_save0_0(10) when exp_save0_0_9 = b"1111111111" else
                             exp_save0_0(10);

  exp_to_shift <= exp_save0_0_plus_one when exp_save0_1(10) = '1' else
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
  trans_3_3: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save3_2 <= exp_save3_1;
      sine_save3_2 <= sine_save3_1;
    end if;
  end process trans_3_3;
  trans_4: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save4 <= exp_save3_2;
      sine_save4 <= sine_save3_2;
    end if;
  end process trans_4;
  trans_5: process (clk) is
  begin
    if (clk'event and clk = '1') then
      exp_save4_0 <= exp_save4;
      exp_save4_1 <= exp_save4;
      exp_save4_2 <= exp_save4;
      exp_save4_3 <= exp_save4;
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
      exp_save5_8 <= exp_save4_2;
      exp_save5_9 <= exp_save4_2;
      exp_save5_10 <= exp_save4_2;
      exp_save5_11 <= exp_save4_2;
      exp_save5_12 <= exp_save4_3;
      exp_save5_13 <= exp_save4_3;
      exp_save5_14 <= exp_save4_3;
      exp_save5_15 <= exp_save4_3;
      sine_save6 <= sine_save5;
      int_tmp <= int_tmp_0;
      int_tmp_inv <= int_tmp_inv_0;
    end if;
  end process trans_6;
  log_shift_left_dptoi_64 : component log_shift_left_dptoi
    port map(
      data_in => mant1, 
      data_out => int_tmp_0,
      shift_cnt => exp_to_shift(5 downto 0),
      clk => clk
    );
  int_tmp_inv_0 <= invert_bits(int_tmp_0 & '0')(63 downto 0);
  save_mid_result: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      --if exp_save5(6 downto 5) /= b"00" then --in this case we have over or underflow
      --  vec_int <= x"00000000";          --how do we make it crash?
      --elsif exp_save5(7) = '1' then
      --vec_int(63) <= sine_save6;
      sine_save7 <= sine_save6;
      if exp_save5_0(10) = '1' then
        vec_int_tmp(3 downto 0) <= int_tmp(3 downto 0);
      else
        vec_int_tmp(3 downto 0) <= int_tmp_inv(3 downto 0);
      end if;
      if exp_save5_1(10) = '1' then
        vec_int_tmp(7 downto 4) <= int_tmp(7 downto 4);
      else
        vec_int_tmp(7 downto 4) <= int_tmp_inv(7 downto 4);
      end if;
      if exp_save5_2(10) = '1' then
        vec_int_tmp(11 downto 8) <= int_tmp(11 downto 8);
      else
        vec_int_tmp(11 downto 8) <= int_tmp_inv(11 downto 8);
      end if;
      if exp_save5_3(10) = '1' then
        vec_int_tmp(15 downto 12) <= int_tmp(15 downto 12);
      else
        vec_int_tmp(15 downto 12) <= int_tmp_inv(15 downto 12);
      end if;
      if exp_save5_4(10) = '1' then
        vec_int_tmp(19 downto 16) <= int_tmp(19 downto 16);
      else
        vec_int_tmp(19 downto 16) <= int_tmp_inv(19 downto 16);
      end if;
      if exp_save5_5(10) = '1' then
        vec_int_tmp(23 downto 20) <= int_tmp(23 downto 20);
      else
        vec_int_tmp(23 downto 20) <= int_tmp_inv(23 downto 20);
      end if;
      if exp_save5_6(10) = '1' then
        vec_int_tmp(27 downto 24) <= int_tmp(27 downto 24);
      else
        vec_int_tmp(27 downto 24) <= int_tmp_inv(27 downto 24);
      end if;
      if exp_save5_7(10) = '1' then
        vec_int_tmp(31 downto 28) <= int_tmp(31 downto 28);
      else
        vec_int_tmp(31 downto 28) <= int_tmp_inv(31 downto 28);
      end if;
      if exp_save5_8(10) = '1' then
        vec_int_tmp(35 downto 32) <= int_tmp(35 downto 32);
      else
        vec_int_tmp(35 downto 32) <= int_tmp_inv(35 downto 32);
      end if;
      if exp_save5_9(10) = '1' then
        vec_int_tmp(39 downto 36) <= int_tmp(39 downto 36);
      else
        vec_int_tmp(39 downto 36) <= int_tmp_inv(39 downto 36);
      end if;
      if exp_save5_10(10) = '1' then
        vec_int_tmp(43 downto 40) <= int_tmp(43 downto 40);
      else
        vec_int_tmp(43 downto 40) <= int_tmp_inv(43 downto 40);
      end if;
      if exp_save5_11(10) = '1' then
        vec_int_tmp(47 downto 44) <= int_tmp(47 downto 44);
      else
        vec_int_tmp(47 downto 44) <= int_tmp_inv(47 downto 44);
      end if;
      if exp_save5_12(10) = '1' then
        vec_int_tmp(51 downto 48) <= int_tmp(51 downto 48);
      else
        vec_int_tmp(51 downto 48) <= int_tmp_inv(51 downto 48);
      end if;
      if exp_save5_13(10) = '1' then
        vec_int_tmp(55 downto 52) <= int_tmp(55 downto 52);
      else
        vec_int_tmp(55 downto 52) <= int_tmp_inv(55 downto 52);
      end if;
      if exp_save5_14(10) = '1' then
        vec_int_tmp(59 downto 56) <= int_tmp(59 downto 56);
      else
        vec_int_tmp(59 downto 56) <= int_tmp_inv(59 downto 56);
      end if;
      if exp_save5_15(10) = '1' then
        vec_int_tmp(62 downto 60) <= int_tmp(62 downto 60);
      else
        vec_int_tmp(62 downto 60) <= int_tmp_inv(62 downto 60);
      end if;
    end if;
  end process save_mid_result;

  bit_64_block_next_reg <= vec_int_tmp;
  conv_sin_reg_0 <= sine_save7;


  bit_16_block_0 <= break_apart_0(bit_64_block_next_reg);
  bit_16_block_1 <= break_apart_1(bit_64_block_next_reg);
  bit_16_block_2 <= break_apart_2(bit_64_block_next_reg);
  bit_16_block_3 <= break_apart_3(bit_64_block_next_reg);


  bit_16_block_0_zero_flag <= not ( bit_16_block_0(0) or 
	  	 	 	   bit_16_block_0(1) or 
	  	 	 	   bit_16_block_0(2) or 
	  	 	 	   bit_16_block_0(3) or 
	  	 	 	   bit_16_block_0(4) or 
	  	 	 	   bit_16_block_0(5) or 
	  	 	 	   bit_16_block_0(6) or 
	  	 	 	   bit_16_block_0(7) or 
	  	 	 	   bit_16_block_0(8) or 
	  	 	 	   bit_16_block_0(9) or 
	  	 	 	   bit_16_block_0(10) or 
	  	 	 	   bit_16_block_0(11) or 
	  	 	 	   bit_16_block_0(12) or 
	  	 	 	   bit_16_block_0(13) or 
	  	 	 	   bit_16_block_0(14) or 
	  	 	 	   bit_16_block_0(15)); 
  bit_16_block_1_zero_flag <= not ( bit_16_block_1(0) or 
	  	 	 	   bit_16_block_1(1) or 
	  	 	 	   bit_16_block_1(2) or 
	  	 	 	   bit_16_block_1(3) or 
	  	 	 	   bit_16_block_1(4) or 
	  	 	 	   bit_16_block_1(5) or 
	  	 	 	   bit_16_block_1(6) or 
	  	 	 	   bit_16_block_1(7) or 
	  	 	 	   bit_16_block_1(8) or 
	  	 	 	   bit_16_block_1(9) or 
	  	 	 	   bit_16_block_1(10) or 
	  	 	 	   bit_16_block_1(11) or 
	  	 	 	   bit_16_block_1(12) or 
	  	 	 	   bit_16_block_1(13) or 
	  	 	 	   bit_16_block_1(14) or 
	  	 	 	   bit_16_block_1(15)); 
  bit_16_block_2_zero_flag <= not ( bit_16_block_2(0) or 
	  	 	 	   bit_16_block_2(1) or 
	  	 	 	   bit_16_block_2(2) or 
	  	 	 	   bit_16_block_2(3) or 
	  	 	 	   bit_16_block_2(4) or 
	  	 	 	   bit_16_block_2(5) or 
	  	 	 	   bit_16_block_2(6) or 
	  	 	 	   bit_16_block_2(7) or 
	  	 	 	   bit_16_block_2(8) or 
	  	 	 	   bit_16_block_2(9) or 
	  	 	 	   bit_16_block_2(10) or 
	  	 	 	   bit_16_block_2(11) or 
	  	 	 	   bit_16_block_2(12) or 
	  	 	 	   bit_16_block_2(13) or 
	  	 	 	   bit_16_block_2(14) or 
	  	 	 	   bit_16_block_2(15)); 


  conv_to_non_neg_0: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_zero_flag_reg <= bit_16_block_0_zero_flag;
      bit_16_block_0_reg <= bit_16_block_0;
      bit_16_block_1_zero_flag_reg <= bit_16_block_1_zero_flag;
      bit_16_block_1_reg <= bit_16_block_1;
      bit_16_block_2_zero_flag_reg <= bit_16_block_2_zero_flag;
      bit_16_block_2_reg <= bit_16_block_2;
      bit_16_block_3_reg <= bit_16_block_3;
      conv_sin_reg_1 <= conv_sin_reg_0;
      bit_16_block_0_zero_flag_reg_1 <= bit_16_block_0_zero_flag;
      bit_16_block_1_zero_flag_reg_1 <= bit_16_block_1_zero_flag;
      bit_16_block_2_zero_flag_reg_1 <= bit_16_block_2_zero_flag;
      vec_int_tmp_0 <= vec_int_tmp;
    end if;
  end process conv_to_non_neg_0;


  bit_16_block_0_new <= bit_16_block_0_reg;
  bit_16_block_1_new <= bit_16_block_1_reg when (bit_16_block_0_zero_flag_reg='1') else 
		       exor(bit_16_block_1_reg, conv_sin_reg_1); 
  bit_16_block_2_new <= bit_16_block_2_reg when (bit_16_block_0_zero_flag_reg='1') and 
				      (bit_16_block_1_zero_flag_reg='1') else 
		       exor(bit_16_block_2_reg, conv_sin_reg_1); 
  bit_16_block_3_new <= bit_16_block_3_reg when (bit_16_block_0_zero_flag_reg='1') and 
				      (bit_16_block_1_zero_flag_reg='1') and 
				      (bit_16_block_2_zero_flag_reg='1') else 
		       exor(bit_16_block_3_reg, conv_sin_reg_1); 


  bit_16_block_next <= bit_16_block_0_new when not (bit_16_block_0_zero_flag_reg='1') else
                  bit_16_block_1_new when not (bit_16_block_1_zero_flag_reg='1') else
                  bit_16_block_2_new when not (bit_16_block_2_zero_flag_reg='1') else
                  bit_16_block_3_new;


  conv_to_non_neg_1: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_new_reg_1 <= bit_16_block_0_new;
      bit_16_block_0_zero_flag_reg_2 <= bit_16_block_0_zero_flag_reg_1;
      bit_16_block_1_new_reg_1 <= bit_16_block_1_new;
      bit_16_block_1_zero_flag_reg_2 <= bit_16_block_1_zero_flag_reg_1;
      bit_16_block_2_new_reg_1 <= bit_16_block_2_new;
      bit_16_block_2_zero_flag_reg_2 <= bit_16_block_2_zero_flag_reg_1;
      bit_16_block_3_new_reg_1 <= bit_16_block_3_new;
      conv_sin_reg_2 <= conv_sin_reg_1;
      bit_16_block_next_reg <= bit_16_block_next;
      vec_int_tmp_1 <= vec_int_tmp_0;
    end if;
  end process conv_to_non_neg_1;


  bit_4_block_0 <= break_apart_0(bit_16_block_next_reg);
  bit_4_block_1 <= break_apart_1(bit_16_block_next_reg);
  bit_4_block_2 <= break_apart_2(bit_16_block_next_reg);
  bit_4_block_3 <= break_apart_3(bit_16_block_next_reg);


  bit_4_block_0_zero_flag <= not ( bit_4_block_0(0) or 
	  	 	 	   bit_4_block_0(1) or 
	  	 	 	   bit_4_block_0(2) or 
	  	 	 	   bit_4_block_0(3)); 
  bit_4_block_1_zero_flag <= not ( bit_4_block_1(0) or 
	  	 	 	   bit_4_block_1(1) or 
	  	 	 	   bit_4_block_1(2) or 
	  	 	 	   bit_4_block_1(3)); 
  bit_4_block_2_zero_flag <= not ( bit_4_block_2(0) or 
	  	 	 	   bit_4_block_2(1) or 
	  	 	 	   bit_4_block_2(2) or 
	  	 	 	   bit_4_block_2(3)); 


  conv_to_non_neg_2: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_4_block_0_zero_flag_reg <= bit_4_block_0_zero_flag;
      bit_4_block_0_reg <= bit_4_block_0;
      bit_4_block_1_zero_flag_reg <= bit_4_block_1_zero_flag;
      bit_4_block_1_reg <= bit_4_block_1;
      bit_4_block_2_zero_flag_reg <= bit_4_block_2_zero_flag;
      bit_4_block_2_reg <= bit_4_block_2;
      bit_4_block_3_reg <= bit_4_block_3;
      conv_sin_reg_3 <= conv_sin_reg_2;
      bit_16_block_0_new_reg_2 <= bit_16_block_0_new_reg_1;
      bit_16_block_0_zero_flag_reg_3 <= bit_16_block_0_zero_flag_reg_2;
      bit_16_block_1_new_reg_2 <= bit_16_block_1_new_reg_1;
      bit_16_block_1_zero_flag_reg_3 <= bit_16_block_1_zero_flag_reg_2;
      bit_16_block_2_new_reg_2 <= bit_16_block_2_new_reg_1;
      bit_16_block_2_zero_flag_reg_3 <= bit_16_block_2_zero_flag_reg_2;
      bit_16_block_3_new_reg_2 <= bit_16_block_3_new_reg_1;
      bit_4_block_0_zero_flag_reg_1 <= bit_4_block_0_zero_flag;
      bit_4_block_1_zero_flag_reg_1 <= bit_4_block_1_zero_flag;
      bit_4_block_2_zero_flag_reg_1 <= bit_4_block_2_zero_flag;
      vec_int_tmp_2 <= vec_int_tmp_1;
    end if;
  end process conv_to_non_neg_2;


  bit_4_block_0_new <= bit_4_block_0_reg;
  bit_4_block_1_new <= bit_4_block_1_reg when (bit_4_block_0_zero_flag_reg='1') else 
		       exor(bit_4_block_1_reg, conv_sin_reg_3); 
  bit_4_block_2_new <= bit_4_block_2_reg when (bit_4_block_0_zero_flag_reg='1') and 
				      (bit_4_block_1_zero_flag_reg='1') else 
		       exor(bit_4_block_2_reg, conv_sin_reg_3); 
  bit_4_block_3_new <= bit_4_block_3_reg when (bit_4_block_0_zero_flag_reg='1') and 
				      (bit_4_block_1_zero_flag_reg='1') and 
				      (bit_4_block_2_zero_flag_reg='1') else 
		       exor(bit_4_block_3_reg, conv_sin_reg_3); 


  bit_4_block_next <= bit_4_block_0_new when not (bit_4_block_0_zero_flag_reg='1') else
                  bit_4_block_1_new when not (bit_4_block_1_zero_flag_reg='1') else
                  bit_4_block_2_new when not (bit_4_block_2_zero_flag_reg='1') else
                  bit_4_block_3_new;


  conv_to_non_neg_3: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_new_reg_3 <= bit_16_block_0_new_reg_2;
      bit_16_block_0_zero_flag_reg_4 <= bit_16_block_0_zero_flag_reg_3;
      bit_16_block_1_new_reg_3 <= bit_16_block_1_new_reg_2;
      bit_16_block_1_zero_flag_reg_4 <= bit_16_block_1_zero_flag_reg_3;
      bit_16_block_2_new_reg_3 <= bit_16_block_2_new_reg_2;
      bit_16_block_2_zero_flag_reg_4 <= bit_16_block_2_zero_flag_reg_3;
      bit_16_block_3_new_reg_3 <= bit_16_block_3_new_reg_2;
      bit_4_block_0_new_reg_1 <= bit_4_block_0_new;
      bit_4_block_0_zero_flag_reg_2 <= bit_4_block_0_zero_flag_reg_1;
      bit_4_block_1_new_reg_1 <= bit_4_block_1_new;
      bit_4_block_1_zero_flag_reg_2 <= bit_4_block_1_zero_flag_reg_1;
      bit_4_block_2_new_reg_1 <= bit_4_block_2_new;
      bit_4_block_2_zero_flag_reg_2 <= bit_4_block_2_zero_flag_reg_1;
      bit_4_block_3_new_reg_1 <= bit_4_block_3_new;
      conv_sin_reg_4 <= conv_sin_reg_3;
      bit_4_block_next_reg <= bit_4_block_next;
      vec_int_tmp_3 <= vec_int_tmp_2;
    end if;
  end process conv_to_non_neg_3;


  bit_1_block_0 <= break_apart_0_1_bit(bit_4_block_next_reg);
  bit_1_block_1 <= break_apart_1_1_bit(bit_4_block_next_reg);
  bit_1_block_2 <= break_apart_2_1_bit(bit_4_block_next_reg);
  bit_1_block_3 <= break_apart_3_1_bit(bit_4_block_next_reg);


  bit_1_block_0_zero_flag <= not (bit_1_block_0);
  bit_1_block_1_zero_flag <= not (bit_1_block_1);
  bit_1_block_2_zero_flag <= not (bit_1_block_2);


  conv_to_non_neg_4: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_1_block_0_zero_flag_reg <= bit_1_block_0_zero_flag;
      bit_1_block_0_reg <= bit_1_block_0;
      bit_1_block_1_zero_flag_reg <= bit_1_block_1_zero_flag;
      bit_1_block_1_reg <= bit_1_block_1;
      bit_1_block_2_zero_flag_reg <= bit_1_block_2_zero_flag;
      bit_1_block_2_reg <= bit_1_block_2;
      bit_1_block_3_reg <= bit_1_block_3;
      conv_sin_reg_5 <= conv_sin_reg_4;
      bit_16_block_0_new_reg_4 <= bit_16_block_0_new_reg_3;
      bit_16_block_0_zero_flag_reg_5 <= bit_16_block_0_zero_flag_reg_4;
      bit_16_block_1_new_reg_4 <= bit_16_block_1_new_reg_3;
      bit_16_block_1_zero_flag_reg_5 <= bit_16_block_1_zero_flag_reg_4;
      bit_16_block_2_new_reg_4 <= bit_16_block_2_new_reg_3;
      bit_16_block_2_zero_flag_reg_5 <= bit_16_block_2_zero_flag_reg_4;
      bit_16_block_3_new_reg_4 <= bit_16_block_3_new_reg_3;
      bit_1_block_0_zero_flag_reg_1 <= bit_1_block_0_zero_flag;
      bit_1_block_1_zero_flag_reg_1 <= bit_1_block_1_zero_flag;
      bit_1_block_2_zero_flag_reg_1 <= bit_1_block_2_zero_flag;
      bit_4_block_0_new_reg_2 <= bit_4_block_0_new_reg_1;
      bit_4_block_0_zero_flag_reg_3 <= bit_4_block_0_zero_flag_reg_2;
      bit_4_block_1_new_reg_2 <= bit_4_block_1_new_reg_1;
      bit_4_block_1_zero_flag_reg_3 <= bit_4_block_1_zero_flag_reg_2;
      bit_4_block_2_new_reg_2 <= bit_4_block_2_new_reg_1;
      bit_4_block_2_zero_flag_reg_3 <= bit_4_block_2_zero_flag_reg_2;
      bit_4_block_3_new_reg_2 <= bit_4_block_3_new_reg_1;
      vec_int_tmp_4 <= vec_int_tmp_3;
    end if;
  end process conv_to_non_neg_4;


  bit_1_block_0_new <= bit_1_block_0_reg;
  bit_1_block_1_new <= bit_1_block_1_reg when (bit_1_block_0_zero_flag_reg='1') else 
		       (bit_1_block_1_reg and not conv_sin_reg_5) or (not bit_1_block_1_reg and conv_sin_reg_5); 
  bit_1_block_2_new <= bit_1_block_2_reg when (bit_1_block_0_zero_flag_reg='1') and 
				      (bit_1_block_1_zero_flag_reg='1') else 
		       (bit_1_block_2_reg and not conv_sin_reg_5) or (not bit_1_block_2_reg and conv_sin_reg_5); 
  bit_1_block_3_new <= bit_1_block_3_reg when (bit_1_block_0_zero_flag_reg='1') and 
				      (bit_1_block_1_zero_flag_reg='1') and 
				      (bit_1_block_2_zero_flag_reg='1') else 
		       (bit_1_block_3_reg and not conv_sin_reg_5) or (not bit_1_block_3_reg and conv_sin_reg_5); 


  bit_1_block_next <= bit_1_block_0_new when not (bit_1_block_0_zero_flag_reg='1') else
                  bit_1_block_1_new when not (bit_1_block_1_zero_flag_reg='1') else
                  bit_1_block_2_new when not (bit_1_block_2_zero_flag_reg='1') else
                  bit_1_block_3_new;


  conv_to_non_neg_5: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_new_reg_5 <= bit_16_block_0_new_reg_4;
      bit_16_block_0_zero_flag_reg_6 <= bit_16_block_0_zero_flag_reg_5;
      bit_16_block_1_new_reg_5 <= bit_16_block_1_new_reg_4;
      bit_16_block_1_zero_flag_reg_6 <= bit_16_block_1_zero_flag_reg_5;
      bit_16_block_2_new_reg_5 <= bit_16_block_2_new_reg_4;
      bit_16_block_2_zero_flag_reg_6 <= bit_16_block_2_zero_flag_reg_5;
      bit_16_block_3_new_reg_5 <= bit_16_block_3_new_reg_4;
      bit_1_block_0_new_reg_1 <= bit_1_block_0_new;
      bit_1_block_0_zero_flag_reg_2 <= bit_1_block_0_zero_flag_reg_1;
      bit_1_block_1_new_reg_1 <= bit_1_block_1_new;
      bit_1_block_1_zero_flag_reg_2 <= bit_1_block_1_zero_flag_reg_1;
      bit_1_block_2_new_reg_1 <= bit_1_block_2_new;
      bit_1_block_2_zero_flag_reg_2 <= bit_1_block_2_zero_flag_reg_1;
      bit_1_block_3_new_reg_1 <= bit_1_block_3_new;
      bit_4_block_0_new_reg_3 <= bit_4_block_0_new_reg_2;
      bit_4_block_0_zero_flag_reg_4 <= bit_4_block_0_zero_flag_reg_3;
      bit_4_block_1_new_reg_3 <= bit_4_block_1_new_reg_2;
      bit_4_block_1_zero_flag_reg_4 <= bit_4_block_1_zero_flag_reg_3;
      bit_4_block_2_new_reg_3 <= bit_4_block_2_new_reg_2;
      bit_4_block_2_zero_flag_reg_4 <= bit_4_block_2_zero_flag_reg_3;
      bit_4_block_3_new_reg_3 <= bit_4_block_3_new_reg_2;
      conv_sin_reg_6 <= conv_sin_reg_5;
      bit_1_block_next_reg <= bit_1_block_next;
      vec_int_tmp_5 <= vec_int_tmp_4;
    end if;
  end process conv_to_non_neg_5;


  bit_1_block_next_reg_0 <= bit_1_block_next_reg;
  bit_1_block_next_reg_1 <= bit_1_block_next_reg;
  bit_1_block_next_reg_2 <= bit_1_block_next_reg;
  bit_1_block_next_reg_3 <= bit_1_block_next_reg;
  
  bit_1_block_0_final <= bit_1_block_next_reg_0 when not (bit_1_block_0_zero_flag_reg_2='1') else
                         bit_1_block_0_new_reg_1;
  bit_1_block_1_final <= bit_1_block_next_reg_1 when (bit_1_block_0_zero_flag_reg_2='1') and
						not (bit_1_block_1_zero_flag_reg_2='1') else
                         bit_1_block_1_new_reg_1;
  bit_1_block_2_final <= bit_1_block_next_reg_2 when (bit_1_block_0_zero_flag_reg_2='1') and
						(bit_1_block_1_zero_flag_reg_2='1') and
						not (bit_1_block_2_zero_flag_reg_2='1') else
                         bit_1_block_2_new_reg_1;
  bit_1_block_3_final <= bit_1_block_next_reg_3 when (bit_1_block_0_zero_flag_reg_2='1') and
						(bit_1_block_1_zero_flag_reg_2='1') and
						(bit_1_block_2_zero_flag_reg_2='1') else
                         bit_1_block_3_new_reg_1;
  bit_1_block_0_final_0 <= bit_1_block_0_final;
  bit_1_block_0_final_1 <= bit_1_block_0_final;
  bit_1_block_0_final_2 <= bit_1_block_0_final;
  bit_1_block_0_final_3 <= bit_1_block_0_final;
  bit_1_block_1_final_0 <= bit_1_block_1_final;
  bit_1_block_1_final_1 <= bit_1_block_1_final;
  bit_1_block_1_final_2 <= bit_1_block_1_final;
  bit_1_block_1_final_3 <= bit_1_block_1_final;
  bit_1_block_2_final_0 <= bit_1_block_2_final;
  bit_1_block_2_final_1 <= bit_1_block_2_final;
  bit_1_block_2_final_2 <= bit_1_block_2_final;
  bit_1_block_2_final_3 <= bit_1_block_2_final;
  bit_1_block_3_final_0 <= bit_1_block_3_final;
  bit_1_block_3_final_1 <= bit_1_block_3_final;
  bit_1_block_3_final_2 <= bit_1_block_3_final;
  bit_1_block_3_final_3 <= bit_1_block_3_final;

  bit_4_block_0_final <= bit_1_block_3_final_0 & bit_1_block_2_final_0 & bit_1_block_1_final_0 & bit_1_block_0_final_0 when not (bit_4_block_0_zero_flag_reg_4='1') else
                         bit_4_block_0_new_reg_3;
  bit_4_block_1_final <= bit_1_block_3_final_1 & bit_1_block_2_final_1 & bit_1_block_1_final_1 & bit_1_block_0_final_1 when (bit_4_block_0_zero_flag_reg_4='1') and
                                              not (bit_4_block_1_zero_flag_reg_4='1') else
                         bit_4_block_1_new_reg_3;
  bit_4_block_2_final <= bit_1_block_3_final_2 & bit_1_block_2_final_2 & bit_1_block_1_final_2 & bit_1_block_0_final_2 when (bit_4_block_0_zero_flag_reg_4='1') and
                                              (bit_4_block_1_zero_flag_reg_4='1') and
                                              not (bit_4_block_2_zero_flag_reg_4='1') else
                         bit_4_block_2_new_reg_3;
  bit_4_block_3_final <= bit_1_block_3_final_3 & bit_1_block_2_final_3 & bit_1_block_1_final_3 & bit_1_block_0_final_3 when (bit_4_block_0_zero_flag_reg_4='1') and
                                              (bit_4_block_1_zero_flag_reg_4='1') and
                                              (bit_4_block_2_zero_flag_reg_4='1') else
                         bit_4_block_3_new_reg_3;
  bit_4_block_0_final_0 <= bit_4_block_0_final;
  bit_4_block_0_final_1 <= bit_4_block_0_final;
  bit_4_block_0_final_2 <= bit_4_block_0_final;
  bit_4_block_0_final_3 <= bit_4_block_0_final;
  bit_4_block_1_final_0 <= bit_4_block_1_final;
  bit_4_block_1_final_1 <= bit_4_block_1_final;
  bit_4_block_1_final_2 <= bit_4_block_1_final;
  bit_4_block_1_final_3 <= bit_4_block_1_final;
  bit_4_block_2_final_0 <= bit_4_block_2_final;
  bit_4_block_2_final_1 <= bit_4_block_2_final;
  bit_4_block_2_final_2 <= bit_4_block_2_final;
  bit_4_block_2_final_3 <= bit_4_block_2_final;
  bit_4_block_3_final_0 <= bit_4_block_3_final;
  bit_4_block_3_final_1 <= bit_4_block_3_final;
  bit_4_block_3_final_2 <= bit_4_block_3_final;
  bit_4_block_3_final_3 <= bit_4_block_3_final;
  
  bit_16_block_0_zero_flag_reg_6_0 <= bit_16_block_0_zero_flag_reg_6;
  bit_16_block_0_zero_flag_reg_6_1 <= bit_16_block_0_zero_flag_reg_6;
  bit_16_block_0_zero_flag_reg_6_2 <= bit_16_block_0_zero_flag_reg_6;
  bit_16_block_0_zero_flag_reg_6_3 <= bit_16_block_0_zero_flag_reg_6;
  bit_16_block_1_zero_flag_reg_6_0 <= bit_16_block_1_zero_flag_reg_6;
  bit_16_block_1_zero_flag_reg_6_1 <= bit_16_block_1_zero_flag_reg_6;
  bit_16_block_1_zero_flag_reg_6_2 <= bit_16_block_1_zero_flag_reg_6;
  bit_16_block_2_zero_flag_reg_6_0 <= bit_16_block_2_zero_flag_reg_6;
  bit_16_block_2_zero_flag_reg_6_1 <= bit_16_block_2_zero_flag_reg_6;
  
  bit_16_block_0_final <= bit_4_block_3_final_0 & bit_4_block_2_final_0 & bit_4_block_1_final_0 & bit_4_block_0_final_0 when not (bit_16_block_0_zero_flag_reg_6_0='1') else
                         bit_16_block_0_new_reg_5;
  bit_16_block_1_final <= bit_4_block_3_final_1 & bit_4_block_2_final_1 & bit_4_block_1_final_1 & bit_4_block_0_final_1 when (bit_16_block_0_zero_flag_reg_6_1='1') and
                                              not (bit_16_block_1_zero_flag_reg_6_0='1') else
                         bit_16_block_1_new_reg_5;
  bit_16_block_2_final <= bit_4_block_3_final_2 & bit_4_block_2_final_2 & bit_4_block_1_final_2 & bit_4_block_0_final_2 when (bit_16_block_0_zero_flag_reg_6_2='1') and
                                              (bit_16_block_1_zero_flag_reg_6_1='1') and
                                              not (bit_16_block_2_zero_flag_reg_6_0='1') else
                         bit_16_block_2_new_reg_5;
  bit_16_block_3_final <= bit_4_block_3_final_3 & bit_4_block_2_final_3 & bit_4_block_1_final_3 & bit_4_block_0_final_3 when (bit_16_block_0_zero_flag_reg_6_3='1') and
                                              (bit_16_block_1_zero_flag_reg_6_2='1') and
                                              (bit_16_block_2_zero_flag_reg_6_1='1') else
                         bit_16_block_3_new_reg_5;


  bit_64_block_0_final <= bit_16_block_3_final & bit_16_block_2_final & bit_16_block_1_final & bit_16_block_0_final;

  save_final_result: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      if(conv_sin_reg_6 =  '1') then
        vec_int(62 downto 0) <= bit_64_block_0_final(62 downto 0);
        vec_int(63) <= conv_sin_reg_6;
      else
        vec_int(62 downto 0) <= vec_int_tmp_5(62 downto 0);
        vec_int(63) <= conv_sin_reg_6;
      end if;
    end if;
  end process save_final_result;
   
end architecture behavioural;
