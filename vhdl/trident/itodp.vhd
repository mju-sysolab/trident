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
-- I could only get this to 166.97278MHz


library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity mux64 is
  generic (constant pinmap : in integer);
  port(output: out std_logic;
       clk: in std_logic;
       sel: in std_logic_vector(5 downto 0);
       inputs: in std_logic_vector(63 downto 0));
end entity mux64;

architecture mux64_arch of mux64 is
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
    signal mux4_0_out: std_logic_vector(0 to 15);
    signal mux4_0_out_reg: std_logic_vector(0 to 15);
    signal mux4_1_out: std_logic_vector(0 to 3);
    signal mux4_1_out_reg: std_logic_vector(0 to 3);
    signal sel_0: std_logic_vector(5 downto 0);
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
    inputbuffer(63 downto pin0) <= inputs(pinmap downto 0);
    inputbuffer(pin0 - 1 downto 0) <= (others => '0');
    sel_0 <= sel;
    sel_0_0 <= sel;
    sel_0_1 <= sel;
    sel_0_2 <= sel;
    sel_0_3 <= sel;
    sel_0_4 <= sel;
    sel_0_5 <= sel;
    sel_0_6 <= sel;
    sel_0_7 <= sel;
    end if;
  end process input_reg;


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

entity log_shift_left is
  port(data_in: in std_logic_vector(63 downto 0);
       shift_cnt: in std_logic_vector(5 downto 0);
       data_out: out std_logic_vector(63 downto 0);
       clk: in std_logic);
end entity log_shift_left;

architecture log_shift_left_arch of log_shift_left is
   component mux64 is
     generic (constant pinmap : in integer);
     port(output: out std_logic;
          clk: in std_logic;
          sel: in std_logic_vector(5 downto 0);
          inputs: in std_logic_vector(63 downto 0));
   end component mux64;
   signal out_buffer: std_logic_vector(63 downto 0);
   signal input_buffer: std_logic_vector(63 downto 0);
   signal input_buffer_0: std_logic_vector(63 downto 0);
   signal input_buffer_0_wire: std_logic_vector(63 downto 0);
   signal input_buffer_1: std_logic_vector(63 downto 0);
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
  input_buffer_0_wire <= input_buffer;
  input_buffer_1_wire <= input_buffer;
  input_buffer_2_wire <= input_buffer;
  input_buffer_3_wire <= input_buffer;
  input_buffer_4_wire <= input_buffer;
  input_buffer_5_wire <= input_buffer;
  input_buffer_6_wire <= input_buffer;
  input_buffer_7_wire <= input_buffer;
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
	shift_cnt_0 <= shift_cnt_reg;
	shift_cnt_1 <= shift_cnt_reg;
	shift_cnt_2 <= shift_cnt_reg;
	shift_cnt_3 <= shift_cnt_reg;
	shift_cnt_4 <= shift_cnt_reg;
	shift_cnt_5 <= shift_cnt_reg;
	shift_cnt_6 <= shift_cnt_reg;
	shift_cnt_7 <= shift_cnt_reg;
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
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_0(63 downto 0),
	             sel => shift_cnt_0);
  end generate foreachpin_0;
  foreachpin_1: for pin0 in 8 to 15 generate
  begin
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_1(63 downto 0),
	             sel => shift_cnt_1);
  end generate foreachpin_1;
  foreachpin_2: for pin0 in 16 to 23 generate
  begin
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_2(63 downto 0),
	             sel => shift_cnt_2);
  end generate foreachpin_2;
  foreachpin_3: for pin0 in 24 to 31 generate
  begin
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_3(63 downto 0),
	             sel => shift_cnt_3);
  end generate foreachpin_3;
  foreachpin_4: for pin0 in 32 to 39 generate
  begin
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_4(63 downto 0),
	             sel => shift_cnt_4);
  end generate foreachpin_4;
  foreachpin_5: for pin0 in 40 to 47 generate
  begin
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_5(63 downto 0),
	             sel => shift_cnt_5);
  end generate foreachpin_5;
  foreachpin_6: for pin0 in 48 to 55 generate
  begin
	mux64_shift: component mux64
	  generic map (pinmap => pin0)
	  port map ( output => out_buffer(pin0),
	             clk => clk,
		     inputs(63 downto 0) => 
		     	  input_buffer_6(63 downto 0),
	             sel => shift_cnt_6);
  end generate foreachpin_6;
  foreachpin_7: for pin0 in 56 to 63 generate
  begin
	mux64_shift: component mux64
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

entity i_to_dp is
  port(vec_int: in std_logic_vector(63 downto 0);
       vec_dp:out std_logic_vector(63 downto 0);
       clk: in std_logic);
end entity i_to_dp;

architecture behavioural of i_to_dp is

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
	function break_apart_3_lg (input: std_logic_vector) 
	              return std_logic is
		variable ans : std_logic; 
	begin
		ans := input(3);
		return ans; 
	end function break_apart_3_lg;
	--function 
	function break_apart_2_lg (input: std_logic_vector) 
	              return std_logic is
		variable ans : std_logic; 
	begin
		ans := input(2);
		return ans; 
	end function break_apart_2_lg;
	--function 
	function break_apart_1_lg (input: std_logic_vector) 
	              return std_logic is
		variable ans : std_logic; 
	begin
		ans := input(1);
		return ans; 
	end function break_apart_1_lg;
	--function 
	function break_apart_0_lg (input: std_logic_vector) 
	              return std_logic is
		variable ans : std_logic; 
	begin
		ans := input(0);
		return ans; 
	end function break_apart_0_lg;
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

    component log_shift_left is
      port(data_in: in std_logic_vector(63 downto 0);
           shift_cnt: in std_logic_vector(5 downto 0);
           data_out: out std_logic_vector(63 downto 0);
	   clk: in std_logic);
    end component log_shift_left;
    
    
    signal mant: std_logic_vector(51 downto 0);
    signal sine: std_logic;
    signal sine_0: std_logic;
    signal sine_1: std_logic;
    signal sine_1_0: std_logic;
    signal sine_1_1: std_logic;
    signal sine_2: std_logic;
    signal sine_2_0: std_logic;
    signal sine_3: std_logic;
    --signal sine_4: std_logic;
    --signal sine_5: std_logic;
    --signal sine_6: std_logic;
    signal sine_7: std_logic;
    signal sine_8: std_logic;
    signal sine_9: std_logic;
    signal sine_10: std_logic;
    signal sine_11: std_logic;
    signal sine_12: std_logic;
    signal temp_buffer: std_logic_vector(63 downto 0);
    signal temp_buffer_0: std_logic_vector(63 downto 0);
    signal temp_buffer_1: std_logic_vector(63 downto 0);
    signal temp_buffer_1_0: std_logic_vector(63 downto 0);
    signal temp_buffer_1_1: std_logic_vector(63 downto 0);
    signal temp_buffer_2: std_logic_vector(63 downto 0);
    signal temp_buffer_2_0: std_logic_vector(63 downto 0);
    signal temp_buffer_3: std_logic_vector(63 downto 0);
    --signal temp_buffer_4: std_logic_vector(63 downto 0);
    --signal temp_buffer_5: std_logic_vector(63 downto 0);
    --signal temp_buffer_6: std_logic_vector(63 downto 0);
    signal temp_buffer_7: std_logic_vector(63 downto 0);
    signal temp_buffer_8: std_logic_vector(63 downto 0);
    signal temp_buffer_9: std_logic_vector(63 downto 0);
    signal temp_buffer_10: std_logic_vector(63 downto 0);
    signal temp_buffer_11: std_logic_vector(63 downto 0);
    signal temp_buffer_12: std_logic_vector(63 downto 0);
    --signal temp_int_5_t: std_logic_vector(31 downto 0);
    --signal temp_int_5_b: std_logic_vector(31 downto 0);
    signal temp_int_4_3: std_logic_vector(15 downto 0);
    signal temp_int_4_3_reg: std_logic_vector(15 downto 0);
    signal temp_int_4_3_reg_0: std_logic_vector(15 downto 0);
    signal temp_int_4_3_cond: std_logic;
    signal temp_int_4_3_cond_reg: std_logic;
    signal temp_int_4_3_cond_0: std_logic;
    signal temp_int_4_3_cond_1: std_logic;
    signal temp_int_4_3_cond_2: std_logic;
    signal temp_int_4_3_cond_3: std_logic;
    signal temp_int_4_3_cond_4: std_logic;
    signal temp_int_4_3_cond_5: std_logic;
    signal temp_int_4_3_cond_reg_0: std_logic;
    signal temp_int_4_3_cond_reg_1: std_logic;
    signal temp_int_4_3_cond_reg_2: std_logic;
    signal temp_int_4_3_cond_reg_3: std_logic;
    signal temp_int_4_3_cond_reg_4: std_logic;
    signal temp_int_4_3_cond_reg_5: std_logic;
    signal temp_int_4_2: std_logic_vector(15 downto 0);
    signal temp_int_4_2_reg: std_logic_vector(15 downto 0);
    signal temp_int_4_2_reg_0: std_logic_vector(15 downto 0);
    signal temp_int_4_2_cond: std_logic;
    signal temp_int_4_2_cond_reg: std_logic;
    signal temp_int_4_2_cond_0: std_logic;
    signal temp_int_4_2_cond_1: std_logic;
    signal temp_int_4_2_cond_2: std_logic;
    signal temp_int_4_2_cond_3: std_logic;
    signal temp_int_4_2_cond_4: std_logic;
    signal temp_int_4_2_cond_5: std_logic;
    signal temp_int_4_2_cond_reg_0: std_logic;
    signal temp_int_4_2_cond_reg_1: std_logic;
    signal temp_int_4_2_cond_reg_2: std_logic;
    signal temp_int_4_2_cond_reg_3: std_logic;
    signal temp_int_4_2_cond_reg_4: std_logic;
    signal temp_int_4_2_cond_reg_5: std_logic;
    signal temp_int_4_1: std_logic_vector(15 downto 0);
    signal temp_int_4_1_reg: std_logic_vector(15 downto 0);
    signal temp_int_4_1_reg_0: std_logic_vector(15 downto 0);
    signal temp_int_4_1_cond: std_logic;
    signal temp_int_4_1_cond_reg: std_logic;
    signal temp_int_4_1_cond_0: std_logic;
    signal temp_int_4_1_cond_1: std_logic;
    signal temp_int_4_1_cond_2: std_logic;
    signal temp_int_4_1_cond_3: std_logic;
    signal temp_int_4_1_cond_4: std_logic;
    signal temp_int_4_1_cond_5: std_logic;
    signal temp_int_4_1_cond_reg_0: std_logic;
    signal temp_int_4_1_cond_reg_1: std_logic;
    signal temp_int_4_1_cond_reg_2: std_logic;
    signal temp_int_4_1_cond_reg_3: std_logic;
    signal temp_int_4_1_cond_reg_4: std_logic;
    signal temp_int_4_1_cond_reg_5: std_logic;
    signal temp_int_4_0: std_logic_vector(15 downto 0);
    signal temp_int_4_0_reg: std_logic_vector(15 downto 0);
    signal temp_int_4_0_reg_0: std_logic_vector(15 downto 0);
    --signal temp_int_3_t: std_logic_vector(7 downto 0);
    --signal temp_int_3_b: std_logic_vector(7 downto 0);
    signal temp_int_2_3: std_logic_vector(3 downto 0);
    signal temp_int_2_3_wire: std_logic_vector(3 downto 0);
    signal temp_int_2_3_reg: std_logic_vector(3 downto 0);
    signal temp_int_2_3_cond: std_logic;
    signal temp_int_2_3_cond_reg: std_logic;
    signal temp_int_2_2: std_logic_vector(3 downto 0);
    signal temp_int_2_2_wire: std_logic_vector(3 downto 0);
    signal temp_int_2_2_reg: std_logic_vector(3 downto 0);
    signal temp_int_2_2_cond: std_logic;
    signal temp_int_2_2_cond_reg: std_logic;
    signal temp_int_2_1: std_logic_vector(3 downto 0);
    signal temp_int_2_1_wire: std_logic_vector(3 downto 0);
    signal temp_int_2_1_reg: std_logic_vector(3 downto 0);
    signal temp_int_2_1_cond: std_logic;
    signal temp_int_2_1_cond_reg: std_logic;
    signal temp_int_2_0: std_logic_vector(3 downto 0);
    signal temp_int_2_0_wire: std_logic_vector(3 downto 0);
    signal temp_int_2_0_reg: std_logic_vector(3 downto 0);
    --signal temp_int_1_t: std_logic_vector(1 downto 0);
    --signal temp_int_1_b: std_logic_vector(1 downto 0);
    signal temp_int_0_3: std_logic;
    signal temp_int_0_3_wire: std_logic;
    signal temp_int_0_2: std_logic;
    signal temp_int_0_2_wire: std_logic;
    signal temp_int_0_1: std_logic;
    signal temp_int_0_1_wire: std_logic;
    signal temp_int_0_0: std_logic;
    signal temp_int_0_0_wire: std_logic;
    signal shift_cnt: std_logic_vector(5 downto 0);
    signal shift_cnt_wire: std_logic_vector(1 downto 0);
    signal shift_cnt_2: std_logic_vector(5 downto 0);
    signal shift_cnt_2_0: std_logic_vector(5 downto 0);
    signal shift_cnt_2_wire: std_logic_vector(1 downto 0);
    signal shift_cnt_3: std_logic_vector(5 downto 0);
    signal shift_cnt_3_wire: std_logic_vector(1 downto 0);
    --signal shift_cnt_4: std_logic_vector(5 downto 0);
    --signal shift_cnt_5: std_logic_vector(5 downto 0);
    --signal shift_cnt_6: std_logic_vector(5 downto 0);
    signal exp: std_logic_vector(10 downto 0);
    signal exp_wire: std_logic_vector(1 downto 0);
    signal exp_2: std_logic_vector(10 downto 0);
    signal exp_2_0: std_logic_vector(10 downto 0);
    signal exp_2_wire: std_logic_vector(1 downto 0);
    signal exp_3: std_logic_vector(10 downto 0);
    signal exp_3_wire: std_logic_vector(1 downto 0);
    --signal exp_4: std_logic_vector(10 downto 0);
    --signal exp_5: std_logic_vector(10 downto 0);
    --signal exp_6: std_logic_vector(10 downto 0);
    signal temp: std_logic_vector(9 downto 0);
    signal temp2: std_logic;
    signal exp_out: std_logic_vector(10 downto 0);
    signal exp_out_reg_0: std_logic_vector(10 downto 0);
    signal exp_out_reg_1: std_logic_vector(10 downto 0);
    signal exp_out_1: std_logic_vector(10 downto 0);
    signal exp_out_2: std_logic_vector(10 downto 0);
    signal exp_out_3: std_logic_vector(10 downto 0);
    signal exp_out_4: std_logic_vector(10 downto 0);
    signal exp_out_5: std_logic_vector(10 downto 0);
    
    --convert out of 2's compliment signals:
    signal bit_16_block_0_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_1_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_2_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_3_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_1_reg_exor: std_logic_vector(15 downto 0);
    signal bit_16_block_2_reg_exor: std_logic_vector(15 downto 0);
    signal bit_16_block_3_reg_exor: std_logic_vector(15 downto 0);
    signal bit_16_block_1_reg_exor_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_2_reg_exor_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_3_reg_exor_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_0_zero_flag_reg_1_reg: std_logic;
    signal bit_16_block_1_zero_flag_reg_1_reg: std_logic;
    signal bit_16_block_2_zero_flag_reg_1_reg: std_logic;
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
    signal temp_buffer_cpy: std_logic_vector(63 downto 0);

    signal bit_16_block_0: std_logic_vector(15 downto 0);
    signal bit_16_block_0_final: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_0: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_2: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_3: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_4: std_logic_vector(15 downto 0);
    signal bit_16_block_0_new_reg_5: std_logic_vector(15 downto 0);
    signal bit_16_block_0_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_0_zero_flag: std_logic;
    signal bit_16_block_0_zero_flag_reg: std_logic;
    signal bit_16_block_0_zero_flag_reg_fan_0: std_logic;
    signal bit_16_block_0_zero_flag_reg_fan_1: std_logic;
    signal bit_16_block_0_zero_flag_reg_fan_2: std_logic;
    signal bit_16_block_0_zero_flag_reg_1: std_logic;
    signal bit_16_block_0_zero_flag_reg_2: std_logic;
    signal bit_16_block_0_zero_flag_reg_2_0: std_logic;
    signal bit_16_block_0_zero_flag_reg_2_0_fan_0: std_logic;
    signal bit_16_block_0_zero_flag_reg_2_0_fan_1: std_logic;
    signal bit_16_block_0_zero_flag_reg_2_0_fan_2: std_logic;
    signal bit_16_block_0_zero_flag_reg_2_0_fan_3: std_logic;
    signal bit_16_block_0_zero_flag_reg_3: std_logic;
    signal bit_16_block_0_zero_flag_reg_4: std_logic;
    signal bit_16_block_0_zero_flag_reg_5: std_logic;
    signal bit_16_block_0_zero_flag_reg_6: std_logic;
    signal bit_16_block_1: std_logic_vector(15 downto 0);
    signal bit_16_block_1_final: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_0: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_1: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_2: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_3: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_4: std_logic_vector(15 downto 0);
    signal bit_16_block_1_new_reg_5: std_logic_vector(15 downto 0);
    signal bit_16_block_1_reg: std_logic_vector(15 downto 0);
    signal bit_16_block_1_zero_flag: std_logic;
    signal bit_16_block_1_zero_flag_reg: std_logic;
    signal bit_16_block_1_zero_flag_reg_fan_0: std_logic;
    signal bit_16_block_1_zero_flag_reg_fan_1: std_logic;
    signal bit_16_block_1_zero_flag_reg_1: std_logic;
    signal bit_16_block_1_zero_flag_reg_2: std_logic;
    signal bit_16_block_1_zero_flag_reg_2_0: std_logic;
    signal bit_16_block_1_zero_flag_reg_2_0_fan_0: std_logic;
    signal bit_16_block_1_zero_flag_reg_2_0_fan_1: std_logic;
    signal bit_16_block_1_zero_flag_reg_2_0_fan_2: std_logic;
    signal bit_16_block_1_zero_flag_reg_2_0_fan_3: std_logic;
    signal bit_16_block_1_zero_flag_reg_3: std_logic;
    signal bit_16_block_1_zero_flag_reg_4: std_logic;
    signal bit_16_block_1_zero_flag_reg_5: std_logic;
    signal bit_16_block_1_zero_flag_reg_6: std_logic;
    signal bit_16_block_2: std_logic_vector(15 downto 0);
    signal bit_16_block_2_final: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new: std_logic_vector(15 downto 0);
    signal bit_16_block_2_new_reg_0: std_logic_vector(15 downto 0);
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
    signal bit_16_block_2_zero_flag_reg_2_0: std_logic;
    signal bit_16_block_2_zero_flag_reg_2_0_fan_0: std_logic;
    signal bit_16_block_2_zero_flag_reg_2_0_fan_1: std_logic;
    signal bit_16_block_2_zero_flag_reg_2_0_fan_2: std_logic;
    signal bit_16_block_2_zero_flag_reg_2_0_fan_3: std_logic;
    signal bit_16_block_2_zero_flag_reg_3: std_logic;
    signal bit_16_block_2_zero_flag_reg_4: std_logic;
    signal bit_16_block_2_zero_flag_reg_5: std_logic;
    signal bit_16_block_2_zero_flag_reg_6: std_logic;
    signal bit_16_block_3: std_logic_vector(15 downto 0);
    signal bit_16_block_3_final: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new: std_logic_vector(15 downto 0);
    signal bit_16_block_3_new_reg_0: std_logic_vector(15 downto 0);
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
    signal conv_sin_reg_1_reg: std_logic;
    signal conv_sin_reg_1_0: std_logic;
    signal conv_sin_reg_1_1: std_logic;
    signal conv_sin_reg_1_2: std_logic;
    signal conv_sin_reg_2: std_logic;
    signal conv_sin_reg_2_0: std_logic;
    signal conv_sin_reg_3: std_logic;
    signal conv_sin_reg_4: std_logic;
    signal conv_sin_reg_5: std_logic;
    signal conv_sin_reg_6: std_logic;
    
    --rounding signals:
    signal mant_rndup_10_1_flag: std_logic;
    signal mant_rndup_11_1_flag: std_logic;
    signal mant_rndup_12_1_flag: std_logic;
    signal mant_rndup_13_1_flag: std_logic;
    signal mant_rndup_14_1_flag: std_logic;
    signal mant_rndup_15_1_flag: std_logic;
    signal mant_rndup_16_1_flag: std_logic;
    signal mant_rndup_17_1_flag: std_logic;
    signal mant_rndup_18_1_flag: std_logic;
    signal mant_rndup_19_1_flag: std_logic;
    signal mant_rndup_1_1_flag: std_logic;
    signal mant_rndup_20_1_flag: std_logic;
    signal mant_rndup_21_1_flag: std_logic;
    signal mant_rndup_22_1_flag: std_logic;
    signal mant_rndup_23_1_flag: std_logic;
    signal mant_rndup_24_1_flag: std_logic;
    signal mant_rndup_25_1_flag: std_logic;
    signal mant_rndup_26_1_flag: std_logic;
    signal mant_rndup_27_1_flag: std_logic;
    signal mant_rndup_28_1_flag: std_logic;
    signal mant_rndup_29_1_flag: std_logic;
    signal mant_rndup_2_1_flag: std_logic;
    signal mant_rndup_30_1_flag: std_logic;
    signal mant_rndup_31_1_flag: std_logic;
    signal mant_rndup_32_1_flag: std_logic;
    signal mant_rndup_33_1_flag: std_logic;
    signal mant_rndup_34_1_flag: std_logic;
    signal mant_rndup_35_1_flag: std_logic;
    signal mant_rndup_36_1_flag: std_logic;
    signal mant_rndup_37_1_flag: std_logic;
    signal mant_rndup_38_1_flag: std_logic;
    signal mant_rndup_39_1_flag: std_logic;
    signal mant_rndup_3_1_flag: std_logic;
    signal mant_rndup_40_1_flag: std_logic;
    signal mant_rndup_41_1_flag: std_logic;
    signal mant_rndup_42_1_flag: std_logic;
    signal mant_rndup_43_1_flag: std_logic;
    signal mant_rndup_44_1_flag: std_logic;
    signal mant_rndup_45_1_flag: std_logic;
    signal mant_rndup_46_1_flag: std_logic;
    signal mant_rndup_47_1_flag: std_logic;
    signal mant_rndup_48_1_flag: std_logic;
    signal mant_rndup_49_1_flag: std_logic;
    signal mant_rndup_4_1_flag: std_logic;
    signal mant_rndup_50_1_flag: std_logic;
    signal mant_rndup_51_1_flag: std_logic;
    signal mant_rndup_5_1_flag: std_logic;
    signal mant_rndup_6_1_flag: std_logic;
    signal mant_rndup_7_1_flag: std_logic;
    signal mant_rndup_8_1_flag: std_logic;
    signal mant_rndup_9_1_flag: std_logic;
    signal mant_0: std_logic_vector(51 downto 0);
    signal mant_1: std_logic_vector(51 downto 0);
    signal mant_10_reg: std_logic_vector(9 downto 0);
    signal mant_11_reg: std_logic_vector(10 downto 0);
    signal mant_12_reg: std_logic_vector(11 downto 0);
    signal mant_13_reg: std_logic_vector(12 downto 0);
    signal mant_14_reg: std_logic_vector(13 downto 0);
    signal mant_15_reg: std_logic_vector(14 downto 0);
    signal mant_16_reg: std_logic_vector(15 downto 0);
    signal mant_17_reg: std_logic_vector(16 downto 0);
    signal mant_18_reg: std_logic_vector(17 downto 0);
    signal mant_19_reg: std_logic_vector(18 downto 0);
    signal mant_1_reg: std_logic;
    signal mant_2: std_logic_vector(51 downto 0);
    signal mant_20_reg: std_logic_vector(19 downto 0);
    signal mant_21_reg: std_logic_vector(20 downto 0);
    signal mant_22_reg: std_logic_vector(21 downto 0);
    signal mant_23_reg: std_logic_vector(22 downto 0);
    signal mant_24_reg: std_logic_vector(23 downto 0);
    signal mant_25_reg: std_logic_vector(24 downto 0);
    signal mant_26_reg: std_logic_vector(25 downto 0);
    signal mant_27_reg: std_logic_vector(26 downto 0);
    signal mant_28_reg: std_logic_vector(27 downto 0);
    signal mant_29_reg: std_logic_vector(28 downto 0);
    signal mant_2_reg: std_logic_vector(1 downto 0);
    signal mant_3: std_logic_vector(51 downto 0);
    signal mant_30_reg: std_logic_vector(29 downto 0);
    signal mant_31_reg: std_logic_vector(30 downto 0);
    signal mant_32_reg: std_logic_vector(31 downto 0);
    signal mant_33_reg: std_logic_vector(32 downto 0);
    signal mant_34_reg: std_logic_vector(33 downto 0);
    signal mant_35_reg: std_logic_vector(34 downto 0);
    signal mant_36_reg: std_logic_vector(35 downto 0);
    signal mant_37_reg: std_logic_vector(36 downto 0);
    signal mant_38_reg: std_logic_vector(37 downto 0);
    signal mant_39_reg: std_logic_vector(38 downto 0);
    signal mant_3_reg: std_logic_vector(2 downto 0);
    signal mant_4: std_logic_vector(51 downto 0);
    signal mant_40_reg: std_logic_vector(39 downto 0);
    signal mant_41_reg: std_logic_vector(40 downto 0);
    signal mant_42_reg: std_logic_vector(41 downto 0);
    signal mant_43_reg: std_logic_vector(42 downto 0);
    signal mant_44_reg: std_logic_vector(43 downto 0);
    signal mant_45_reg: std_logic_vector(44 downto 0);
    signal mant_46_reg: std_logic_vector(45 downto 0);
    signal mant_47_reg: std_logic_vector(46 downto 0);
    signal mant_48_reg: std_logic_vector(47 downto 0);
    signal mant_49_reg: std_logic_vector(48 downto 0);
    signal mant_4_reg: std_logic_vector(3 downto 0);
    signal mant_5: std_logic_vector(51 downto 0);
    signal mant_50_reg: std_logic_vector(49 downto 0);
    signal mant_51_reg: std_logic_vector(50 downto 0);
    signal mant_5_reg: std_logic_vector(4 downto 0);
    signal mant_6: std_logic_vector(51 downto 0);
    signal mant_6_reg: std_logic_vector(5 downto 0);
    signal mant_7: std_logic_vector(51 downto 0);
    signal mant_7_reg: std_logic_vector(6 downto 0);
    signal mant_8: std_logic_vector(51 downto 0);
    
    signal mant_0_fan: std_logic_vector(51 downto 0);
    signal mant_1_fan: std_logic_vector(51 downto 0);
    signal mant_2_fan: std_logic_vector(51 downto 0);
    signal mant_3_fan: std_logic_vector(51 downto 0);
    signal mant_4_fan: std_logic_vector(51 downto 0);
    signal mant_5_fan: std_logic_vector(51 downto 0);
    signal mant_6_fan: std_logic_vector(51 downto 0);
    signal mant_7_fan: std_logic_vector(51 downto 0);
    signal mant_8_fan: std_logic_vector(51 downto 0);
    
    signal mant_8_reg: std_logic_vector(7 downto 0);
    signal mant_9_reg: std_logic_vector(8 downto 0);
    signal mant_rndup: std_logic_vector(51 downto 0);
    signal mant_rndup_reg: std_logic_vector(51 downto 0);
    signal mant_reg: std_logic_vector(51 downto 0);
    signal mant_reg_0: std_logic_vector(51 downto 0);
    signal mant_reg_1: std_logic_vector(51 downto 0);
    signal mant_reg_2: std_logic_vector(51 downto 0);
    signal roundup_flag: std_logic;
    signal roundup_flag_reg_0_0: std_logic;
    signal roundup_flag_reg_0: std_logic;
    signal roundup_flag_reg: std_logic;
    signal exp_out_4_rndup_10_1_flag: std_logic;
    signal exp_out_4_rndup_1_1_flag: std_logic;
    signal exp_out_4_rndup_2_1_flag: std_logic;
    signal exp_out_4_rndup_3_1_flag: std_logic;
    signal exp_out_4_rndup_4_1_flag: std_logic;
    signal exp_out_4_rndup_5_1_flag: std_logic;
    signal exp_out_4_rndup_6_1_flag: std_logic;
    signal exp_out_4_rndup_7_1_flag: std_logic;
    signal exp_out_4_rndup_8_1_flag: std_logic;
    signal exp_out_4_rndup_9_1_flag: std_logic;
    signal exp_out_4_0: std_logic_vector(10 downto 0);
    
    signal exp_out_4_0_fan: std_logic_vector(10 downto 0);
    signal exp_out_4_1_fan: std_logic_vector(10 downto 0);
    signal exp_out_4_2_fan: std_logic_vector(10 downto 0);
    signal exp_out_4_3_fan: std_logic_vector(10 downto 0);
    
    signal exp_out_4_1: std_logic_vector(10 downto 0);
    signal exp_out_4_10_reg: std_logic_vector(9 downto 0);
    signal exp_out_4_1_reg: std_logic;
    signal exp_out_4_2: std_logic_vector(10 downto 0);
    signal exp_out_4_2_reg: std_logic_vector(1 downto 0);
    signal exp_out_4_3: std_logic_vector(10 downto 0);
    signal exp_out_4_3_reg: std_logic_vector(2 downto 0);
    signal exp_out_4_4_reg: std_logic_vector(3 downto 0);
    signal exp_out_4_5_reg: std_logic_vector(4 downto 0);
    signal exp_out_4_6_reg: std_logic_vector(5 downto 0);
    signal exp_out_4_7_reg: std_logic_vector(6 downto 0);
    signal exp_out_4_8_reg: std_logic_vector(7 downto 0);
    signal exp_out_4_9_reg: std_logic_vector(8 downto 0);
    signal exp_out_4_rndup: std_logic_vector(10 downto 0);
    signal exp_out_4_rndup_reg: std_logic_vector(10 downto 0);
    signal exp_out_4_reg_0: std_logic_vector(10 downto 0);
    signal exp_out_4_reg_1: std_logic_vector(10 downto 0);
    signal exp_out_4_reg_2: std_logic_vector(10 downto 0);
    signal temp_buffer_12_0: std_logic_vector(63 downto 0);
    signal temp_buffer_13: std_logic_vector(63 downto 0);
    signal temp_buffer_14: std_logic_vector(63 downto 0);
    signal sine_12_0: std_logic;
    signal sine_13: std_logic;
    signal sine_14: std_logic;
    signal exp_out_5_0: std_logic_vector(10 downto 0);
    signal exp_out_6: std_logic_vector(10 downto 0);
    signal exp_out_7: std_logic_vector(10 downto 0);
    
begin
  shftlftlog : component log_shift_left
    port map(
      data_in => temp_buffer_3, 
      data_out(10) => roundup_flag,
      data_out(63) => temp2,
      data_out(62 downto 11) => mant,
      data_out(9 downto 0) => temp,
      shift_cnt => shift_cnt_3,
      clk => clk
    );
  
  
  
  
  exp_add: process (clk)
  begin
    if (clk'event and clk = '1')  then
      --exp_out <= exp_3 + b"1111111111";
      --exp_out(10) <= exp_3(10) xor '1';
      --exp_out(9 downto 0) <= exp_3(9 downto 0);
      exp_out_reg_0 <= exp_3;
      exp_out_reg_1 <= exp_3;
      --exp_out(0) <= not exp_3(0);
      sine_7 <= sine_3;
      temp_buffer_7 <= temp_buffer_3;
    end if;
  end process exp_add;
  
  exp_out(10 downto 1) <= '1' & exp_out_reg_0(9 downto 1) 
                                 when exp_out_reg_1(0) = '1' else
			  '1' & exp_out_reg_0(9 downto 2) & not exp_out_reg_0(1)
			         when exp_out_reg_1(1) = '1' else
			  '1' & exp_out_reg_0(9 downto 3) & not exp_out_reg_0(2 downto 1)
			         when exp_out_reg_1(2) = '1' else
			  '1' & exp_out_reg_0(9 downto 4) & not exp_out_reg_0(3 downto 1)
			         when exp_out_reg_1(3) = '1' else
			  '1' & exp_out_reg_0(9 downto 5) & not exp_out_reg_0(4 downto 1)
			         when exp_out_reg_1(4) = '1' else
			  '1' & exp_out_reg_0(9 downto 6) & not exp_out_reg_0(5 downto 1)
			         when exp_out_reg_1(5) = '1' else
			  '0' & not exp_out_reg_0(9 downto 1);
  exp_out(0) <= not exp_out_reg_0(0);
  
  save_1: process (clk)
  begin
    if (clk'event and clk = '1')  then
      --exp_out_1 <= exp_out - '1';
      exp_out_1 <= exp_out;
      sine_8 <= sine_7;
      temp_buffer_8 <= temp_buffer_7;
    end if;
  end process save_1;
  
  save_2: process (clk)
  begin
    if (clk'event and clk = '1')  then
      exp_out_2 <= exp_out_1;
      sine_9 <= sine_8;
      temp_buffer_9 <= temp_buffer_8;
    end if;
  end process save_2;
  
  save_3: process (clk)
  begin
    if (clk'event and clk = '1')  then
      exp_out_3 <= exp_out_2;
      sine_10 <= sine_9;
      temp_buffer_10 <= temp_buffer_9;
    end if;
  end process save_3;
  
  save_4: process (clk)
  begin
    if (clk'event and clk = '1')  then
      exp_out_4 <= exp_out_3;
      sine_11 <= sine_10;
      temp_buffer_11 <= temp_buffer_10;
    end if;
  end process save_4;
  
  
  mant_0_fan <= mant;
  mant_1_fan <= mant;
  mant_2_fan <= mant;
  mant_3_fan <= mant;
  mant_4_fan <= mant;
  mant_5_fan <= mant;
  mant_6_fan <= mant;
  mant_7_fan <= mant;
  mant_8_fan <= mant;
  exp_out_4_0_fan <= exp_out_5;
  exp_out_4_1_fan <= exp_out_5;
  exp_out_4_2_fan <= exp_out_5;
  exp_out_4_3_fan <= exp_out_5;
  
  save_5_0: process (clk)
  begin
    if (clk'event and clk = '1')  then
      exp_out_5_0 <= exp_out_4;
      sine_12_0 <= sine_11;
      temp_buffer_12_0 <= temp_buffer_11;
      mant_0 <= mant_0_fan;
      mant_1 <= mant_1_fan;
      mant_2 <= mant_2_fan;
      mant_3 <= mant_3_fan;
      mant_4 <= mant_4_fan;
      mant_5 <= mant_5_fan;
      mant_6 <= mant_6_fan;
      mant_7 <= mant_7_fan;
      mant_8 <= mant_8_fan;
      roundup_flag_reg_0_0 <= roundup_flag;
      exp_out_4_0 <= exp_out_4_0_fan;
      exp_out_4_1 <= exp_out_4_1_fan;
      exp_out_4_2 <= exp_out_4_2_fan;
      exp_out_4_3 <= exp_out_4_3_fan;
    end if;
  end process save_5_0;
  

  save_5: process (clk)
  begin
    if (clk'event and clk = '1')  then
      exp_out_5 <= exp_out_5_0;
      sine_12 <= sine_12_0;
      temp_buffer_12 <= temp_buffer_12_0;
      mant_1_reg <= mant_0(0);
      mant_2_reg <= mant_0(1 downto 0);
      mant_3_reg <= mant_0(2 downto 0);
      mant_4_reg <= mant_0(3 downto 0);
      mant_5_reg <= mant_0(4 downto 0);
      mant_6_reg <= mant_0(5 downto 0);
      mant_7_reg <= mant_0(6 downto 0);
      mant_8_reg <= mant_1(7 downto 0);
      mant_9_reg <= mant_1(8 downto 0);
      mant_10_reg <= mant_1(9 downto 0);
      mant_11_reg <= mant_1(10 downto 0);
      mant_12_reg <= mant_1(11 downto 0);
      mant_13_reg <= mant_1(12 downto 0);
      mant_14_reg <= mant_2(13 downto 0);
      mant_15_reg <= mant_2(14 downto 0);
      mant_16_reg <= mant_2(15 downto 0);
      mant_17_reg <= mant_2(16 downto 0);
      mant_18_reg <= mant_2(17 downto 0);
      mant_19_reg <= mant_2(18 downto 0);
      mant_20_reg <= mant_3(19 downto 0);
      mant_21_reg <= mant_3(20 downto 0);
      mant_22_reg <= mant_3(21 downto 0);
      mant_23_reg <= mant_3(22 downto 0);
      mant_24_reg <= mant_3(23 downto 0);
      mant_25_reg <= mant_3(24 downto 0);
      mant_26_reg <= mant_4(25 downto 0);
      mant_27_reg <= mant_4(26 downto 0);
      mant_28_reg <= mant_4(27 downto 0);
      mant_29_reg <= mant_4(28 downto 0);
      mant_30_reg <= mant_4(29 downto 0);
      mant_31_reg <= mant_4(30 downto 0);
      mant_32_reg <= mant_5(31 downto 0);
      mant_33_reg <= mant_5(32 downto 0);
      mant_34_reg <= mant_5(33 downto 0);
      mant_35_reg <= mant_5(34 downto 0);
      mant_36_reg <= mant_5(35 downto 0);
      mant_37_reg <= mant_5(36 downto 0);
      mant_38_reg <= mant_6(37 downto 0);
      mant_39_reg <= mant_6(38 downto 0);
      mant_40_reg <= mant_6(39 downto 0);
      mant_41_reg <= mant_6(40 downto 0);
      mant_42_reg <= mant_6(41 downto 0);
      mant_43_reg <= mant_6(42 downto 0);
      mant_44_reg <= mant_7(43 downto 0);
      mant_45_reg <= mant_7(44 downto 0);
      mant_46_reg <= mant_7(45 downto 0);
      mant_47_reg <= mant_7(46 downto 0);
      mant_48_reg <= mant_7(47 downto 0);
      mant_49_reg <= mant_7(48 downto 0);
      mant_50_reg <= mant_8(49 downto 0);
      mant_51_reg <= mant_8(50 downto 0);
      mant_reg_0 <= mant_8;
      mant_reg_1 <= mant_8;
      mant_reg_2 <= mant_8;
      roundup_flag_reg_0 <= roundup_flag_reg_0_0;
      exp_out_4_1_reg <= exp_out_4_0(0);
      exp_out_4_2_reg <= exp_out_4_0(1 downto 0);
      exp_out_4_3_reg <= exp_out_4_0(2 downto 0);
      exp_out_4_4_reg <= exp_out_4_1(3 downto 0);
      exp_out_4_5_reg <= exp_out_4_1(4 downto 0);
      exp_out_4_6_reg <= exp_out_4_1(5 downto 0);
      exp_out_4_7_reg <= exp_out_4_2(6 downto 0);
      exp_out_4_8_reg <= exp_out_4_2(7 downto 0);
      exp_out_4_9_reg <= exp_out_4_2(8 downto 0);
      exp_out_4_10_reg <= exp_out_4_3(9 downto 0);
      exp_out_4_reg_0 <= exp_out_4_3;
      exp_out_4_reg_1 <= exp_out_4_3;
      exp_out_4_reg_2 <= exp_out_4_3;
    end if;
  end process save_5;
  
  mant_rndup_1_1_flag <= (mant_1_reg);
  mant_rndup_2_1_flag <= (mant_2_reg(0) and mant_2_reg(1));
  mant_rndup_3_1_flag <= (mant_3_reg(0) and mant_3_reg(1) and mant_3_reg(2));
  mant_rndup_4_1_flag <= (mant_4_reg(0) and mant_4_reg(1) and mant_4_reg(2) and mant_4_reg(3));
  mant_rndup_5_1_flag <= (mant_5_reg(0) and mant_5_reg(1) and mant_5_reg(2) and mant_5_reg(3) and mant_5_reg(4));
  mant_rndup_6_1_flag <= (mant_6_reg(0) and mant_6_reg(1) and mant_6_reg(2) and mant_6_reg(3) and mant_6_reg(4) and mant_6_reg(5));
  mant_rndup_7_1_flag <= (mant_7_reg(0) and mant_7_reg(1) and mant_7_reg(2) and mant_7_reg(3) and mant_7_reg(4) and mant_7_reg(5) and mant_7_reg(6));
  mant_rndup_8_1_flag <= (mant_8_reg(0) and mant_8_reg(1) and mant_8_reg(2) and mant_8_reg(3) and mant_8_reg(4) and mant_8_reg(5) and mant_8_reg(6) and mant_8_reg(7));
  mant_rndup_9_1_flag <= (mant_9_reg(0) and mant_9_reg(1) and mant_9_reg(2) and mant_9_reg(3) and mant_9_reg(4) and mant_9_reg(5) and mant_9_reg(6) and mant_9_reg(7) and mant_9_reg(8));
  mant_rndup_10_1_flag <= (mant_10_reg(0) and mant_10_reg(1) and mant_10_reg(2) and mant_10_reg(3) and mant_10_reg(4) and mant_10_reg(5) and mant_10_reg(6) and mant_10_reg(7) and mant_10_reg(8) and mant_10_reg(9));
  mant_rndup_11_1_flag <= (mant_11_reg(0) and mant_11_reg(1) and mant_11_reg(2) and mant_11_reg(3) and mant_11_reg(4) and mant_11_reg(5) and mant_11_reg(6) and mant_11_reg(7) and mant_11_reg(8) and mant_11_reg(9) and mant_11_reg(10));
  mant_rndup_12_1_flag <= (mant_12_reg(0) and mant_12_reg(1) and mant_12_reg(2) and mant_12_reg(3) and mant_12_reg(4) and mant_12_reg(5) and mant_12_reg(6) and mant_12_reg(7) and mant_12_reg(8) and mant_12_reg(9) and mant_12_reg(10) and mant_12_reg(11));
  mant_rndup_13_1_flag <= (mant_13_reg(0) and mant_13_reg(1) and mant_13_reg(2) and mant_13_reg(3) and mant_13_reg(4) and mant_13_reg(5) and mant_13_reg(6) and mant_13_reg(7) and mant_13_reg(8) and mant_13_reg(9) and mant_13_reg(10) and mant_13_reg(11) and mant_13_reg(12));
  mant_rndup_14_1_flag <= (mant_14_reg(0) and mant_14_reg(1) and mant_14_reg(2) and mant_14_reg(3) and mant_14_reg(4) and mant_14_reg(5) and mant_14_reg(6) and mant_14_reg(7) and mant_14_reg(8) and mant_14_reg(9) and mant_14_reg(10) and mant_14_reg(11) and mant_14_reg(12) and mant_14_reg(13));
  mant_rndup_15_1_flag <= (mant_15_reg(0) and mant_15_reg(1) and mant_15_reg(2) and mant_15_reg(3) and mant_15_reg(4) and mant_15_reg(5) and mant_15_reg(6) and mant_15_reg(7) and mant_15_reg(8) and mant_15_reg(9) and mant_15_reg(10) and mant_15_reg(11) and mant_15_reg(12) and mant_15_reg(13) and mant_15_reg(14));
  mant_rndup_16_1_flag <= (mant_16_reg(0) and mant_16_reg(1) and mant_16_reg(2) and mant_16_reg(3) and mant_16_reg(4) and mant_16_reg(5) and mant_16_reg(6) and mant_16_reg(7) and mant_16_reg(8) and mant_16_reg(9) and mant_16_reg(10) and mant_16_reg(11) and mant_16_reg(12) and mant_16_reg(13) and mant_16_reg(14) and mant_16_reg(15));
  mant_rndup_17_1_flag <= (mant_17_reg(0) and mant_17_reg(1) and mant_17_reg(2) and mant_17_reg(3) and mant_17_reg(4) and mant_17_reg(5) and mant_17_reg(6) and mant_17_reg(7) and mant_17_reg(8) and mant_17_reg(9) and mant_17_reg(10) and mant_17_reg(11) and mant_17_reg(12) and mant_17_reg(13) and mant_17_reg(14) and mant_17_reg(15) and mant_17_reg(16));
  mant_rndup_18_1_flag <= (mant_18_reg(0) and mant_18_reg(1) and mant_18_reg(2) and mant_18_reg(3) and mant_18_reg(4) and mant_18_reg(5) and mant_18_reg(6) and mant_18_reg(7) and mant_18_reg(8) and mant_18_reg(9) and mant_18_reg(10) and mant_18_reg(11) and mant_18_reg(12) and mant_18_reg(13) and mant_18_reg(14) and mant_18_reg(15) and mant_18_reg(16) and mant_18_reg(17));
  mant_rndup_19_1_flag <= (mant_19_reg(0) and mant_19_reg(1) and mant_19_reg(2) and mant_19_reg(3) and mant_19_reg(4) and mant_19_reg(5) and mant_19_reg(6) and mant_19_reg(7) and mant_19_reg(8) and mant_19_reg(9) and mant_19_reg(10) and mant_19_reg(11) and mant_19_reg(12) and mant_19_reg(13) and mant_19_reg(14) and mant_19_reg(15) and mant_19_reg(16) and mant_19_reg(17) and mant_19_reg(18));
  mant_rndup_20_1_flag <= (mant_20_reg(0) and mant_20_reg(1) and mant_20_reg(2) and mant_20_reg(3) and mant_20_reg(4) and mant_20_reg(5) and mant_20_reg(6) and mant_20_reg(7) and mant_20_reg(8) and mant_20_reg(9) and mant_20_reg(10) and mant_20_reg(11) and mant_20_reg(12) and mant_20_reg(13) and mant_20_reg(14) and mant_20_reg(15) and mant_20_reg(16) and mant_20_reg(17) and mant_20_reg(18) and mant_20_reg(19));
  mant_rndup_21_1_flag <= (mant_21_reg(0) and mant_21_reg(1) and mant_21_reg(2) and mant_21_reg(3) and mant_21_reg(4) and mant_21_reg(5) and mant_21_reg(6) and mant_21_reg(7) and mant_21_reg(8) and mant_21_reg(9) and mant_21_reg(10) and mant_21_reg(11) and mant_21_reg(12) and mant_21_reg(13) and mant_21_reg(14) and mant_21_reg(15) and mant_21_reg(16) and mant_21_reg(17) and mant_21_reg(18) and mant_21_reg(19) and mant_21_reg(20));
  mant_rndup_22_1_flag <= (mant_22_reg(0) and mant_22_reg(1) and mant_22_reg(2) and mant_22_reg(3) and mant_22_reg(4) and mant_22_reg(5) and mant_22_reg(6) and mant_22_reg(7) and mant_22_reg(8) and mant_22_reg(9) and mant_22_reg(10) and mant_22_reg(11) and mant_22_reg(12) and mant_22_reg(13) and mant_22_reg(14) and mant_22_reg(15) and mant_22_reg(16) and mant_22_reg(17) and mant_22_reg(18) and mant_22_reg(19) and mant_22_reg(20) and mant_22_reg(21));
  mant_rndup_23_1_flag <= (mant_23_reg(0) and mant_23_reg(1) and mant_23_reg(2) and mant_23_reg(3) and mant_23_reg(4) and mant_23_reg(5) and mant_23_reg(6) and mant_23_reg(7) and mant_23_reg(8) and mant_23_reg(9) and mant_23_reg(10) and mant_23_reg(11) and mant_23_reg(12) and mant_23_reg(13) and mant_23_reg(14) and mant_23_reg(15) and mant_23_reg(16) and mant_23_reg(17) and mant_23_reg(18) and mant_23_reg(19) and mant_23_reg(20) and mant_23_reg(21) and mant_23_reg(22));
  mant_rndup_24_1_flag <= (mant_24_reg(0) and mant_24_reg(1) and mant_24_reg(2) and mant_24_reg(3) and mant_24_reg(4) and mant_24_reg(5) and mant_24_reg(6) and mant_24_reg(7) and mant_24_reg(8) and mant_24_reg(9) and mant_24_reg(10) and mant_24_reg(11) and mant_24_reg(12) and mant_24_reg(13) and mant_24_reg(14) and mant_24_reg(15) and mant_24_reg(16) and mant_24_reg(17) and mant_24_reg(18) and mant_24_reg(19) and mant_24_reg(20) and mant_24_reg(21) and mant_24_reg(22) and mant_24_reg(23));
  mant_rndup_25_1_flag <= (mant_25_reg(0) and mant_25_reg(1) and mant_25_reg(2) and mant_25_reg(3) and mant_25_reg(4) and mant_25_reg(5) and mant_25_reg(6) and mant_25_reg(7) and mant_25_reg(8) and mant_25_reg(9) and mant_25_reg(10) and mant_25_reg(11) and mant_25_reg(12) and mant_25_reg(13) and mant_25_reg(14) and mant_25_reg(15) and mant_25_reg(16) and mant_25_reg(17) and mant_25_reg(18) and mant_25_reg(19) and mant_25_reg(20) and mant_25_reg(21) and mant_25_reg(22) and mant_25_reg(23) and mant_25_reg(24));
  mant_rndup_26_1_flag <= (mant_26_reg(0) and mant_26_reg(1) and mant_26_reg(2) and mant_26_reg(3) and mant_26_reg(4) and mant_26_reg(5) and mant_26_reg(6) and mant_26_reg(7) and mant_26_reg(8) and mant_26_reg(9) and mant_26_reg(10) and mant_26_reg(11) and mant_26_reg(12) and mant_26_reg(13) and mant_26_reg(14) and mant_26_reg(15) and mant_26_reg(16) and mant_26_reg(17) and mant_26_reg(18) and mant_26_reg(19) and mant_26_reg(20) and mant_26_reg(21) and mant_26_reg(22) and mant_26_reg(23) and mant_26_reg(24) and mant_26_reg(25));
  mant_rndup_27_1_flag <= (mant_27_reg(0) and mant_27_reg(1) and mant_27_reg(2) and mant_27_reg(3) and mant_27_reg(4) and mant_27_reg(5) and mant_27_reg(6) and mant_27_reg(7) and mant_27_reg(8) and mant_27_reg(9) and mant_27_reg(10) and mant_27_reg(11) and mant_27_reg(12) and mant_27_reg(13) and mant_27_reg(14) and mant_27_reg(15) and mant_27_reg(16) and mant_27_reg(17) and mant_27_reg(18) and mant_27_reg(19) and mant_27_reg(20) and mant_27_reg(21) and mant_27_reg(22) and mant_27_reg(23) and mant_27_reg(24) and mant_27_reg(25) and mant_27_reg(26));
  mant_rndup_28_1_flag <= (mant_28_reg(0) and mant_28_reg(1) and mant_28_reg(2) and mant_28_reg(3) and mant_28_reg(4) and mant_28_reg(5) and mant_28_reg(6) and mant_28_reg(7) and mant_28_reg(8) and mant_28_reg(9) and mant_28_reg(10) and mant_28_reg(11) and mant_28_reg(12) and mant_28_reg(13) and mant_28_reg(14) and mant_28_reg(15) and mant_28_reg(16) and mant_28_reg(17) and mant_28_reg(18) and mant_28_reg(19) and mant_28_reg(20) and mant_28_reg(21) and mant_28_reg(22) and mant_28_reg(23) and mant_28_reg(24) and mant_28_reg(25) and mant_28_reg(26) and mant_28_reg(27));
  mant_rndup_29_1_flag <= (mant_29_reg(0) and mant_29_reg(1) and mant_29_reg(2) and mant_29_reg(3) and mant_29_reg(4) and mant_29_reg(5) and mant_29_reg(6) and mant_29_reg(7) and mant_29_reg(8) and mant_29_reg(9) and mant_29_reg(10) and mant_29_reg(11) and mant_29_reg(12) and mant_29_reg(13) and mant_29_reg(14) and mant_29_reg(15) and mant_29_reg(16) and mant_29_reg(17) and mant_29_reg(18) and mant_29_reg(19) and mant_29_reg(20) and mant_29_reg(21) and mant_29_reg(22) and mant_29_reg(23) and mant_29_reg(24) and mant_29_reg(25) and mant_29_reg(26) and mant_29_reg(27) and mant_29_reg(28));
  mant_rndup_30_1_flag <= (mant_30_reg(0) and mant_30_reg(1) and mant_30_reg(2) and mant_30_reg(3) and mant_30_reg(4) and mant_30_reg(5) and mant_30_reg(6) and mant_30_reg(7) and mant_30_reg(8) and mant_30_reg(9) and mant_30_reg(10) and mant_30_reg(11) and mant_30_reg(12) and mant_30_reg(13) and mant_30_reg(14) and mant_30_reg(15) and mant_30_reg(16) and mant_30_reg(17) and mant_30_reg(18) and mant_30_reg(19) and mant_30_reg(20) and mant_30_reg(21) and mant_30_reg(22) and mant_30_reg(23) and mant_30_reg(24) and mant_30_reg(25) and mant_30_reg(26) and mant_30_reg(27) and mant_30_reg(28) and mant_30_reg(29));
  mant_rndup_31_1_flag <= (mant_31_reg(0) and mant_31_reg(1) and mant_31_reg(2) and mant_31_reg(3) and mant_31_reg(4) and mant_31_reg(5) and mant_31_reg(6) and mant_31_reg(7) and mant_31_reg(8) and mant_31_reg(9) and mant_31_reg(10) and mant_31_reg(11) and mant_31_reg(12) and mant_31_reg(13) and mant_31_reg(14) and mant_31_reg(15) and mant_31_reg(16) and mant_31_reg(17) and mant_31_reg(18) and mant_31_reg(19) and mant_31_reg(20) and mant_31_reg(21) and mant_31_reg(22) and mant_31_reg(23) and mant_31_reg(24) and mant_31_reg(25) and mant_31_reg(26) and mant_31_reg(27) and mant_31_reg(28) and mant_31_reg(29) and mant_31_reg(30));
  mant_rndup_32_1_flag <= (mant_32_reg(0) and mant_32_reg(1) and mant_32_reg(2) and mant_32_reg(3) and mant_32_reg(4) and mant_32_reg(5) and mant_32_reg(6) and mant_32_reg(7) and mant_32_reg(8) and mant_32_reg(9) and mant_32_reg(10) and mant_32_reg(11) and mant_32_reg(12) and mant_32_reg(13) and mant_32_reg(14) and mant_32_reg(15) and mant_32_reg(16) and mant_32_reg(17) and mant_32_reg(18) and mant_32_reg(19) and mant_32_reg(20) and mant_32_reg(21) and mant_32_reg(22) and mant_32_reg(23) and mant_32_reg(24) and mant_32_reg(25) and mant_32_reg(26) and mant_32_reg(27) and mant_32_reg(28) and mant_32_reg(29) and mant_32_reg(30) and mant_32_reg(31));
  mant_rndup_33_1_flag <= (mant_33_reg(0) and mant_33_reg(1) and mant_33_reg(2) and mant_33_reg(3) and mant_33_reg(4) and mant_33_reg(5) and mant_33_reg(6) and mant_33_reg(7) and mant_33_reg(8) and mant_33_reg(9) and mant_33_reg(10) and mant_33_reg(11) and mant_33_reg(12) and mant_33_reg(13) and mant_33_reg(14) and mant_33_reg(15) and mant_33_reg(16) and mant_33_reg(17) and mant_33_reg(18) and mant_33_reg(19) and mant_33_reg(20) and mant_33_reg(21) and mant_33_reg(22) and mant_33_reg(23) and mant_33_reg(24) and mant_33_reg(25) and mant_33_reg(26) and mant_33_reg(27) and mant_33_reg(28) and mant_33_reg(29) and mant_33_reg(30) and mant_33_reg(31) and mant_33_reg(32));
  mant_rndup_34_1_flag <= (mant_34_reg(0) and mant_34_reg(1) and mant_34_reg(2) and mant_34_reg(3) and mant_34_reg(4) and mant_34_reg(5) and mant_34_reg(6) and mant_34_reg(7) and mant_34_reg(8) and mant_34_reg(9) and mant_34_reg(10) and mant_34_reg(11) and mant_34_reg(12) and mant_34_reg(13) and mant_34_reg(14) and mant_34_reg(15) and mant_34_reg(16) and mant_34_reg(17) and mant_34_reg(18) and mant_34_reg(19) and mant_34_reg(20) and mant_34_reg(21) and mant_34_reg(22) and mant_34_reg(23) and mant_34_reg(24) and mant_34_reg(25) and mant_34_reg(26) and mant_34_reg(27) and mant_34_reg(28) and mant_34_reg(29) and mant_34_reg(30) and mant_34_reg(31) and mant_34_reg(32) and mant_34_reg(33));
  mant_rndup_35_1_flag <= (mant_35_reg(0) and mant_35_reg(1) and mant_35_reg(2) and mant_35_reg(3) and mant_35_reg(4) and mant_35_reg(5) and mant_35_reg(6) and mant_35_reg(7) and mant_35_reg(8) and mant_35_reg(9) and mant_35_reg(10) and mant_35_reg(11) and mant_35_reg(12) and mant_35_reg(13) and mant_35_reg(14) and mant_35_reg(15) and mant_35_reg(16) and mant_35_reg(17) and mant_35_reg(18) and mant_35_reg(19) and mant_35_reg(20) and mant_35_reg(21) and mant_35_reg(22) and mant_35_reg(23) and mant_35_reg(24) and mant_35_reg(25) and mant_35_reg(26) and mant_35_reg(27) and mant_35_reg(28) and mant_35_reg(29) and mant_35_reg(30) and mant_35_reg(31) and mant_35_reg(32) and mant_35_reg(33) and mant_35_reg(34));
  mant_rndup_36_1_flag <= (mant_36_reg(0) and mant_36_reg(1) and mant_36_reg(2) and mant_36_reg(3) and mant_36_reg(4) and mant_36_reg(5) and mant_36_reg(6) and mant_36_reg(7) and mant_36_reg(8) and mant_36_reg(9) and mant_36_reg(10) and mant_36_reg(11) and mant_36_reg(12) and mant_36_reg(13) and mant_36_reg(14) and mant_36_reg(15) and mant_36_reg(16) and mant_36_reg(17) and mant_36_reg(18) and mant_36_reg(19) and mant_36_reg(20) and mant_36_reg(21) and mant_36_reg(22) and mant_36_reg(23) and mant_36_reg(24) and mant_36_reg(25) and mant_36_reg(26) and mant_36_reg(27) and mant_36_reg(28) and mant_36_reg(29) and mant_36_reg(30) and mant_36_reg(31) and mant_36_reg(32) and mant_36_reg(33) and mant_36_reg(34) and mant_36_reg(35));
  mant_rndup_37_1_flag <= (mant_37_reg(0) and mant_37_reg(1) and mant_37_reg(2) and mant_37_reg(3) and mant_37_reg(4) and mant_37_reg(5) and mant_37_reg(6) and mant_37_reg(7) and mant_37_reg(8) and mant_37_reg(9) and mant_37_reg(10) and mant_37_reg(11) and mant_37_reg(12) and mant_37_reg(13) and mant_37_reg(14) and mant_37_reg(15) and mant_37_reg(16) and mant_37_reg(17) and mant_37_reg(18) and mant_37_reg(19) and mant_37_reg(20) and mant_37_reg(21) and mant_37_reg(22) and mant_37_reg(23) and mant_37_reg(24) and mant_37_reg(25) and mant_37_reg(26) and mant_37_reg(27) and mant_37_reg(28) and mant_37_reg(29) and mant_37_reg(30) and mant_37_reg(31) and mant_37_reg(32) and mant_37_reg(33) and mant_37_reg(34) and mant_37_reg(35) and mant_37_reg(36));
  mant_rndup_38_1_flag <= (mant_38_reg(0) and mant_38_reg(1) and mant_38_reg(2) and mant_38_reg(3) and mant_38_reg(4) and mant_38_reg(5) and mant_38_reg(6) and mant_38_reg(7) and mant_38_reg(8) and mant_38_reg(9) and mant_38_reg(10) and mant_38_reg(11) and mant_38_reg(12) and mant_38_reg(13) and mant_38_reg(14) and mant_38_reg(15) and mant_38_reg(16) and mant_38_reg(17) and mant_38_reg(18) and mant_38_reg(19) and mant_38_reg(20) and mant_38_reg(21) and mant_38_reg(22) and mant_38_reg(23) and mant_38_reg(24) and mant_38_reg(25) and mant_38_reg(26) and mant_38_reg(27) and mant_38_reg(28) and mant_38_reg(29) and mant_38_reg(30) and mant_38_reg(31) and mant_38_reg(32) and mant_38_reg(33) and mant_38_reg(34) and mant_38_reg(35) and mant_38_reg(36) and mant_38_reg(37));
  mant_rndup_39_1_flag <= (mant_39_reg(0) and mant_39_reg(1) and mant_39_reg(2) and mant_39_reg(3) and mant_39_reg(4) and mant_39_reg(5) and mant_39_reg(6) and mant_39_reg(7) and mant_39_reg(8) and mant_39_reg(9) and mant_39_reg(10) and mant_39_reg(11) and mant_39_reg(12) and mant_39_reg(13) and mant_39_reg(14) and mant_39_reg(15) and mant_39_reg(16) and mant_39_reg(17) and mant_39_reg(18) and mant_39_reg(19) and mant_39_reg(20) and mant_39_reg(21) and mant_39_reg(22) and mant_39_reg(23) and mant_39_reg(24) and mant_39_reg(25) and mant_39_reg(26) and mant_39_reg(27) and mant_39_reg(28) and mant_39_reg(29) and mant_39_reg(30) and mant_39_reg(31) and mant_39_reg(32) and mant_39_reg(33) and mant_39_reg(34) and mant_39_reg(35) and mant_39_reg(36) and mant_39_reg(37) and mant_39_reg(38));
  mant_rndup_40_1_flag <= (mant_40_reg(0) and mant_40_reg(1) and mant_40_reg(2) and mant_40_reg(3) and mant_40_reg(4) and mant_40_reg(5) and mant_40_reg(6) and mant_40_reg(7) and mant_40_reg(8) and mant_40_reg(9) and mant_40_reg(10) and mant_40_reg(11) and mant_40_reg(12) and mant_40_reg(13) and mant_40_reg(14) and mant_40_reg(15) and mant_40_reg(16) and mant_40_reg(17) and mant_40_reg(18) and mant_40_reg(19) and mant_40_reg(20) and mant_40_reg(21) and mant_40_reg(22) and mant_40_reg(23) and mant_40_reg(24) and mant_40_reg(25) and mant_40_reg(26) and mant_40_reg(27) and mant_40_reg(28) and mant_40_reg(29) and mant_40_reg(30) and mant_40_reg(31) and mant_40_reg(32) and mant_40_reg(33) and mant_40_reg(34) and mant_40_reg(35) and mant_40_reg(36) and mant_40_reg(37) and mant_40_reg(38) and mant_40_reg(39));
  mant_rndup_41_1_flag <= (mant_41_reg(0) and mant_41_reg(1) and mant_41_reg(2) and mant_41_reg(3) and mant_41_reg(4) and mant_41_reg(5) and mant_41_reg(6) and mant_41_reg(7) and mant_41_reg(8) and mant_41_reg(9) and mant_41_reg(10) and mant_41_reg(11) and mant_41_reg(12) and mant_41_reg(13) and mant_41_reg(14) and mant_41_reg(15) and mant_41_reg(16) and mant_41_reg(17) and mant_41_reg(18) and mant_41_reg(19) and mant_41_reg(20) and mant_41_reg(21) and mant_41_reg(22) and mant_41_reg(23) and mant_41_reg(24) and mant_41_reg(25) and mant_41_reg(26) and mant_41_reg(27) and mant_41_reg(28) and mant_41_reg(29) and mant_41_reg(30) and mant_41_reg(31) and mant_41_reg(32) and mant_41_reg(33) and mant_41_reg(34) and mant_41_reg(35) and mant_41_reg(36) and mant_41_reg(37) and mant_41_reg(38) and mant_41_reg(39) and mant_41_reg(40));
  mant_rndup_42_1_flag <= (mant_42_reg(0) and mant_42_reg(1) and mant_42_reg(2) and mant_42_reg(3) and mant_42_reg(4) and mant_42_reg(5) and mant_42_reg(6) and mant_42_reg(7) and mant_42_reg(8) and mant_42_reg(9) and mant_42_reg(10) and mant_42_reg(11) and mant_42_reg(12) and mant_42_reg(13) and mant_42_reg(14) and mant_42_reg(15) and mant_42_reg(16) and mant_42_reg(17) and mant_42_reg(18) and mant_42_reg(19) and mant_42_reg(20) and mant_42_reg(21) and mant_42_reg(22) and mant_42_reg(23) and mant_42_reg(24) and mant_42_reg(25) and mant_42_reg(26) and mant_42_reg(27) and mant_42_reg(28) and mant_42_reg(29) and mant_42_reg(30) and mant_42_reg(31) and mant_42_reg(32) and mant_42_reg(33) and mant_42_reg(34) and mant_42_reg(35) and mant_42_reg(36) and mant_42_reg(37) and mant_42_reg(38) and mant_42_reg(39) and mant_42_reg(40) and mant_42_reg(41));
  mant_rndup_43_1_flag <= (mant_43_reg(0) and mant_43_reg(1) and mant_43_reg(2) and mant_43_reg(3) and mant_43_reg(4) and mant_43_reg(5) and mant_43_reg(6) and mant_43_reg(7) and mant_43_reg(8) and mant_43_reg(9) and mant_43_reg(10) and mant_43_reg(11) and mant_43_reg(12) and mant_43_reg(13) and mant_43_reg(14) and mant_43_reg(15) and mant_43_reg(16) and mant_43_reg(17) and mant_43_reg(18) and mant_43_reg(19) and mant_43_reg(20) and mant_43_reg(21) and mant_43_reg(22) and mant_43_reg(23) and mant_43_reg(24) and mant_43_reg(25) and mant_43_reg(26) and mant_43_reg(27) and mant_43_reg(28) and mant_43_reg(29) and mant_43_reg(30) and mant_43_reg(31) and mant_43_reg(32) and mant_43_reg(33) and mant_43_reg(34) and mant_43_reg(35) and mant_43_reg(36) and mant_43_reg(37) and mant_43_reg(38) and mant_43_reg(39) and mant_43_reg(40) and mant_43_reg(41) and mant_43_reg(42));
  mant_rndup_44_1_flag <= (mant_44_reg(0) and mant_44_reg(1) and mant_44_reg(2) and mant_44_reg(3) and mant_44_reg(4) and mant_44_reg(5) and mant_44_reg(6) and mant_44_reg(7) and mant_44_reg(8) and mant_44_reg(9) and mant_44_reg(10) and mant_44_reg(11) and mant_44_reg(12) and mant_44_reg(13) and mant_44_reg(14) and mant_44_reg(15) and mant_44_reg(16) and mant_44_reg(17) and mant_44_reg(18) and mant_44_reg(19) and mant_44_reg(20) and mant_44_reg(21) and mant_44_reg(22) and mant_44_reg(23) and mant_44_reg(24) and mant_44_reg(25) and mant_44_reg(26) and mant_44_reg(27) and mant_44_reg(28) and mant_44_reg(29) and mant_44_reg(30) and mant_44_reg(31) and mant_44_reg(32) and mant_44_reg(33) and mant_44_reg(34) and mant_44_reg(35) and mant_44_reg(36) and mant_44_reg(37) and mant_44_reg(38) and mant_44_reg(39) and mant_44_reg(40) and mant_44_reg(41) and mant_44_reg(42) and mant_44_reg(43));
  mant_rndup_45_1_flag <= (mant_45_reg(0) and mant_45_reg(1) and mant_45_reg(2) and mant_45_reg(3) and mant_45_reg(4) and mant_45_reg(5) and mant_45_reg(6) and mant_45_reg(7) and mant_45_reg(8) and mant_45_reg(9) and mant_45_reg(10) and mant_45_reg(11) and mant_45_reg(12) and mant_45_reg(13) and mant_45_reg(14) and mant_45_reg(15) and mant_45_reg(16) and mant_45_reg(17) and mant_45_reg(18) and mant_45_reg(19) and mant_45_reg(20) and mant_45_reg(21) and mant_45_reg(22) and mant_45_reg(23) and mant_45_reg(24) and mant_45_reg(25) and mant_45_reg(26) and mant_45_reg(27) and mant_45_reg(28) and mant_45_reg(29) and mant_45_reg(30) and mant_45_reg(31) and mant_45_reg(32) and mant_45_reg(33) and mant_45_reg(34) and mant_45_reg(35) and mant_45_reg(36) and mant_45_reg(37) and mant_45_reg(38) and mant_45_reg(39) and mant_45_reg(40) and mant_45_reg(41) and mant_45_reg(42) and mant_45_reg(43) and mant_45_reg(44));
  mant_rndup_46_1_flag <= (mant_46_reg(0) and mant_46_reg(1) and mant_46_reg(2) and mant_46_reg(3) and mant_46_reg(4) and mant_46_reg(5) and mant_46_reg(6) and mant_46_reg(7) and mant_46_reg(8) and mant_46_reg(9) and mant_46_reg(10) and mant_46_reg(11) and mant_46_reg(12) and mant_46_reg(13) and mant_46_reg(14) and mant_46_reg(15) and mant_46_reg(16) and mant_46_reg(17) and mant_46_reg(18) and mant_46_reg(19) and mant_46_reg(20) and mant_46_reg(21) and mant_46_reg(22) and mant_46_reg(23) and mant_46_reg(24) and mant_46_reg(25) and mant_46_reg(26) and mant_46_reg(27) and mant_46_reg(28) and mant_46_reg(29) and mant_46_reg(30) and mant_46_reg(31) and mant_46_reg(32) and mant_46_reg(33) and mant_46_reg(34) and mant_46_reg(35) and mant_46_reg(36) and mant_46_reg(37) and mant_46_reg(38) and mant_46_reg(39) and mant_46_reg(40) and mant_46_reg(41) and mant_46_reg(42) and mant_46_reg(43) and mant_46_reg(44) and mant_46_reg(45));
  mant_rndup_47_1_flag <= (mant_47_reg(0) and mant_47_reg(1) and mant_47_reg(2) and mant_47_reg(3) and mant_47_reg(4) and mant_47_reg(5) and mant_47_reg(6) and mant_47_reg(7) and mant_47_reg(8) and mant_47_reg(9) and mant_47_reg(10) and mant_47_reg(11) and mant_47_reg(12) and mant_47_reg(13) and mant_47_reg(14) and mant_47_reg(15) and mant_47_reg(16) and mant_47_reg(17) and mant_47_reg(18) and mant_47_reg(19) and mant_47_reg(20) and mant_47_reg(21) and mant_47_reg(22) and mant_47_reg(23) and mant_47_reg(24) and mant_47_reg(25) and mant_47_reg(26) and mant_47_reg(27) and mant_47_reg(28) and mant_47_reg(29) and mant_47_reg(30) and mant_47_reg(31) and mant_47_reg(32) and mant_47_reg(33) and mant_47_reg(34) and mant_47_reg(35) and mant_47_reg(36) and mant_47_reg(37) and mant_47_reg(38) and mant_47_reg(39) and mant_47_reg(40) and mant_47_reg(41) and mant_47_reg(42) and mant_47_reg(43) and mant_47_reg(44) and mant_47_reg(45) and mant_47_reg(46));
  mant_rndup_48_1_flag <= (mant_48_reg(0) and mant_48_reg(1) and mant_48_reg(2) and mant_48_reg(3) and mant_48_reg(4) and mant_48_reg(5) and mant_48_reg(6) and mant_48_reg(7) and mant_48_reg(8) and mant_48_reg(9) and mant_48_reg(10) and mant_48_reg(11) and mant_48_reg(12) and mant_48_reg(13) and mant_48_reg(14) and mant_48_reg(15) and mant_48_reg(16) and mant_48_reg(17) and mant_48_reg(18) and mant_48_reg(19) and mant_48_reg(20) and mant_48_reg(21) and mant_48_reg(22) and mant_48_reg(23) and mant_48_reg(24) and mant_48_reg(25) and mant_48_reg(26) and mant_48_reg(27) and mant_48_reg(28) and mant_48_reg(29) and mant_48_reg(30) and mant_48_reg(31) and mant_48_reg(32) and mant_48_reg(33) and mant_48_reg(34) and mant_48_reg(35) and mant_48_reg(36) and mant_48_reg(37) and mant_48_reg(38) and mant_48_reg(39) and mant_48_reg(40) and mant_48_reg(41) and mant_48_reg(42) and mant_48_reg(43) and mant_48_reg(44) and mant_48_reg(45) and mant_48_reg(46) and mant_48_reg(47));
  mant_rndup_49_1_flag <= (mant_49_reg(0) and mant_49_reg(1) and mant_49_reg(2) and mant_49_reg(3) and mant_49_reg(4) and mant_49_reg(5) and mant_49_reg(6) and mant_49_reg(7) and mant_49_reg(8) and mant_49_reg(9) and mant_49_reg(10) and mant_49_reg(11) and mant_49_reg(12) and mant_49_reg(13) and mant_49_reg(14) and mant_49_reg(15) and mant_49_reg(16) and mant_49_reg(17) and mant_49_reg(18) and mant_49_reg(19) and mant_49_reg(20) and mant_49_reg(21) and mant_49_reg(22) and mant_49_reg(23) and mant_49_reg(24) and mant_49_reg(25) and mant_49_reg(26) and mant_49_reg(27) and mant_49_reg(28) and mant_49_reg(29) and mant_49_reg(30) and mant_49_reg(31) and mant_49_reg(32) and mant_49_reg(33) and mant_49_reg(34) and mant_49_reg(35) and mant_49_reg(36) and mant_49_reg(37) and mant_49_reg(38) and mant_49_reg(39) and mant_49_reg(40) and mant_49_reg(41) and mant_49_reg(42) and mant_49_reg(43) and mant_49_reg(44) and mant_49_reg(45) and mant_49_reg(46) and mant_49_reg(47) and mant_49_reg(48));
  mant_rndup_50_1_flag <= (mant_50_reg(0) and mant_50_reg(1) and mant_50_reg(2) and mant_50_reg(3) and mant_50_reg(4) and mant_50_reg(5) and mant_50_reg(6) and mant_50_reg(7) and mant_50_reg(8) and mant_50_reg(9) and mant_50_reg(10) and mant_50_reg(11) and mant_50_reg(12) and mant_50_reg(13) and mant_50_reg(14) and mant_50_reg(15) and mant_50_reg(16) and mant_50_reg(17) and mant_50_reg(18) and mant_50_reg(19) and mant_50_reg(20) and mant_50_reg(21) and mant_50_reg(22) and mant_50_reg(23) and mant_50_reg(24) and mant_50_reg(25) and mant_50_reg(26) and mant_50_reg(27) and mant_50_reg(28) and mant_50_reg(29) and mant_50_reg(30) and mant_50_reg(31) and mant_50_reg(32) and mant_50_reg(33) and mant_50_reg(34) and mant_50_reg(35) and mant_50_reg(36) and mant_50_reg(37) and mant_50_reg(38) and mant_50_reg(39) and mant_50_reg(40) and mant_50_reg(41) and mant_50_reg(42) and mant_50_reg(43) and mant_50_reg(44) and mant_50_reg(45) and mant_50_reg(46) and mant_50_reg(47) and mant_50_reg(48) and mant_50_reg(49));
  mant_rndup_51_1_flag <= (mant_51_reg(0) and mant_51_reg(1) and mant_51_reg(2) and mant_51_reg(3) and mant_51_reg(4) and mant_51_reg(5) and mant_51_reg(6) and mant_51_reg(7) and mant_51_reg(8) and mant_51_reg(9) and mant_51_reg(10) and mant_51_reg(11) and mant_51_reg(12) and mant_51_reg(13) and mant_51_reg(14) and mant_51_reg(15) and mant_51_reg(16) and mant_51_reg(17) and mant_51_reg(18) and mant_51_reg(19) and mant_51_reg(20) and mant_51_reg(21) and mant_51_reg(22) and mant_51_reg(23) and mant_51_reg(24) and mant_51_reg(25) and mant_51_reg(26) and mant_51_reg(27) and mant_51_reg(28) and mant_51_reg(29) and mant_51_reg(30) and mant_51_reg(31) and mant_51_reg(32) and mant_51_reg(33) and mant_51_reg(34) and mant_51_reg(35) and mant_51_reg(36) and mant_51_reg(37) and mant_51_reg(38) and mant_51_reg(39) and mant_51_reg(40) and mant_51_reg(41) and mant_51_reg(42) and mant_51_reg(43) and mant_51_reg(44) and mant_51_reg(45) and mant_51_reg(46) and mant_51_reg(47) and mant_51_reg(48) and mant_51_reg(49) and mant_51_reg(50));

  mant_rndup(0) <= not mant_reg_0(0);
  mant_rndup(1) <= not mant_reg_1(1) when mant_rndup_1_1_flag = '1' else
                    mant_reg_2(1);
  mant_rndup(2) <= not mant_reg_1(2) when mant_rndup_2_1_flag = '1' else
                    mant_reg_2(2);
  mant_rndup(3) <= not mant_reg_1(3) when mant_rndup_3_1_flag = '1' else
                    mant_reg_2(3);
  mant_rndup(4) <= not mant_reg_1(4) when mant_rndup_4_1_flag = '1' else
                    mant_reg_2(4);
  mant_rndup(5) <= not mant_reg_1(5) when mant_rndup_5_1_flag = '1' else
                    mant_reg_2(5);
  mant_rndup(6) <= not mant_reg_1(6) when mant_rndup_6_1_flag = '1' else
                    mant_reg_2(6);
  mant_rndup(7) <= not mant_reg_1(7) when mant_rndup_7_1_flag = '1' else
                    mant_reg_2(7);
  mant_rndup(8) <= not mant_reg_1(8) when mant_rndup_8_1_flag = '1' else
                    mant_reg_2(8);
  mant_rndup(9) <= not mant_reg_1(9) when mant_rndup_9_1_flag = '1' else
                    mant_reg_2(9);
  mant_rndup(10) <= not mant_reg_1(10) when mant_rndup_10_1_flag = '1' else
                    mant_reg_2(10);
  mant_rndup(11) <= not mant_reg_1(11) when mant_rndup_11_1_flag = '1' else
                    mant_reg_2(11);
  mant_rndup(12) <= not mant_reg_1(12) when mant_rndup_12_1_flag = '1' else
                    mant_reg_2(12);
  mant_rndup(13) <= not mant_reg_1(13) when mant_rndup_13_1_flag = '1' else
                    mant_reg_2(13);
  mant_rndup(14) <= not mant_reg_1(14) when mant_rndup_14_1_flag = '1' else
                    mant_reg_2(14);
  mant_rndup(15) <= not mant_reg_1(15) when mant_rndup_15_1_flag = '1' else
                    mant_reg_2(15);
  mant_rndup(16) <= not mant_reg_1(16) when mant_rndup_16_1_flag = '1' else
                    mant_reg_2(16);
  mant_rndup(17) <= not mant_reg_1(17) when mant_rndup_17_1_flag = '1' else
                    mant_reg_2(17);
  mant_rndup(18) <= not mant_reg_1(18) when mant_rndup_18_1_flag = '1' else
                    mant_reg_2(18);
  mant_rndup(19) <= not mant_reg_1(19) when mant_rndup_19_1_flag = '1' else
                    mant_reg_2(19);
  mant_rndup(20) <= not mant_reg_1(20) when mant_rndup_20_1_flag = '1' else
                    mant_reg_2(20);
  mant_rndup(21) <= not mant_reg_1(21) when mant_rndup_21_1_flag = '1' else
                    mant_reg_2(21);
  mant_rndup(22) <= not mant_reg_1(22) when mant_rndup_22_1_flag = '1' else
                    mant_reg_2(22);
  mant_rndup(23) <= not mant_reg_1(23) when mant_rndup_23_1_flag = '1' else
                    mant_reg_2(23);
  mant_rndup(24) <= not mant_reg_1(24) when mant_rndup_24_1_flag = '1' else
                    mant_reg_2(24);
  mant_rndup(25) <= not mant_reg_1(25) when mant_rndup_25_1_flag = '1' else
                    mant_reg_2(25);
  mant_rndup(26) <= not mant_reg_1(26) when mant_rndup_26_1_flag = '1' else
                    mant_reg_2(26);
  mant_rndup(27) <= not mant_reg_1(27) when mant_rndup_27_1_flag = '1' else
                    mant_reg_2(27);
  mant_rndup(28) <= not mant_reg_1(28) when mant_rndup_28_1_flag = '1' else
                    mant_reg_2(28);
  mant_rndup(29) <= not mant_reg_1(29) when mant_rndup_29_1_flag = '1' else
                    mant_reg_2(29);
  mant_rndup(30) <= not mant_reg_1(30) when mant_rndup_30_1_flag = '1' else
                    mant_reg_2(30);
  mant_rndup(31) <= not mant_reg_1(31) when mant_rndup_31_1_flag = '1' else
                    mant_reg_2(31);
  mant_rndup(32) <= not mant_reg_1(32) when mant_rndup_32_1_flag = '1' else
                    mant_reg_2(32);
  mant_rndup(33) <= not mant_reg_1(33) when mant_rndup_33_1_flag = '1' else
                    mant_reg_2(33);
  mant_rndup(34) <= not mant_reg_1(34) when mant_rndup_34_1_flag = '1' else
                    mant_reg_2(34);
  mant_rndup(35) <= not mant_reg_1(35) when mant_rndup_35_1_flag = '1' else
                    mant_reg_2(35);
  mant_rndup(36) <= not mant_reg_1(36) when mant_rndup_36_1_flag = '1' else
                    mant_reg_2(36);
  mant_rndup(37) <= not mant_reg_1(37) when mant_rndup_37_1_flag = '1' else
                    mant_reg_2(37);
  mant_rndup(38) <= not mant_reg_1(38) when mant_rndup_38_1_flag = '1' else
                    mant_reg_2(38);
  mant_rndup(39) <= not mant_reg_1(39) when mant_rndup_39_1_flag = '1' else
                    mant_reg_2(39);
  mant_rndup(40) <= not mant_reg_1(40) when mant_rndup_40_1_flag = '1' else
                    mant_reg_2(40);
  mant_rndup(41) <= not mant_reg_1(41) when mant_rndup_41_1_flag = '1' else
                    mant_reg_2(41);
  mant_rndup(42) <= not mant_reg_1(42) when mant_rndup_42_1_flag = '1' else
                    mant_reg_2(42);
  mant_rndup(43) <= not mant_reg_1(43) when mant_rndup_43_1_flag = '1' else
                    mant_reg_2(43);
  mant_rndup(44) <= not mant_reg_1(44) when mant_rndup_44_1_flag = '1' else
                    mant_reg_2(44);
  mant_rndup(45) <= not mant_reg_1(45) when mant_rndup_45_1_flag = '1' else
                    mant_reg_2(45);
  mant_rndup(46) <= not mant_reg_1(46) when mant_rndup_46_1_flag = '1' else
                    mant_reg_2(46);
  mant_rndup(47) <= not mant_reg_1(47) when mant_rndup_47_1_flag = '1' else
                    mant_reg_2(47);
  mant_rndup(48) <= not mant_reg_1(48) when mant_rndup_48_1_flag = '1' else
                    mant_reg_2(48);
  mant_rndup(49) <= not mant_reg_1(49) when mant_rndup_49_1_flag = '1' else
                    mant_reg_2(49);
  mant_rndup(50) <= not mant_reg_1(50) when mant_rndup_50_1_flag = '1' else
                    mant_reg_2(50);
  mant_rndup(51) <= not mant_reg_1(51) when mant_rndup_51_1_flag = '1' else
                    mant_reg_2(51);
  
  exp_out_4_rndup_1_1_flag <= (exp_out_4_1_reg);
  exp_out_4_rndup_2_1_flag <= (exp_out_4_2_reg(0) and exp_out_4_2_reg(1));
  exp_out_4_rndup_3_1_flag <= (exp_out_4_3_reg(0) and exp_out_4_3_reg(1) and exp_out_4_3_reg(2));
  exp_out_4_rndup_4_1_flag <= (exp_out_4_4_reg(0) and exp_out_4_4_reg(1) and exp_out_4_4_reg(2) and exp_out_4_4_reg(3));
  exp_out_4_rndup_5_1_flag <= (exp_out_4_5_reg(0) and exp_out_4_5_reg(1) and exp_out_4_5_reg(2) and exp_out_4_5_reg(3) and exp_out_4_5_reg(4));
  exp_out_4_rndup_6_1_flag <= (exp_out_4_6_reg(0) and exp_out_4_6_reg(1) and exp_out_4_6_reg(2) and exp_out_4_6_reg(3) and exp_out_4_6_reg(4) and exp_out_4_6_reg(5));
  exp_out_4_rndup_7_1_flag <= (exp_out_4_7_reg(0) and exp_out_4_7_reg(1) and exp_out_4_7_reg(2) and exp_out_4_7_reg(3) and exp_out_4_7_reg(4) and exp_out_4_7_reg(5) and exp_out_4_7_reg(6));
  exp_out_4_rndup_8_1_flag <= (exp_out_4_8_reg(0) and exp_out_4_8_reg(1) and exp_out_4_8_reg(2) and exp_out_4_8_reg(3) and exp_out_4_8_reg(4) and exp_out_4_8_reg(5) and exp_out_4_8_reg(6) and exp_out_4_8_reg(7));
  exp_out_4_rndup_9_1_flag <= (exp_out_4_9_reg(0) and exp_out_4_9_reg(1) and exp_out_4_9_reg(2) and exp_out_4_9_reg(3) and exp_out_4_9_reg(4) and exp_out_4_9_reg(5) and exp_out_4_9_reg(6) and exp_out_4_9_reg(7) and exp_out_4_9_reg(8));
  exp_out_4_rndup_10_1_flag <= (exp_out_4_10_reg(0) and exp_out_4_10_reg(1) and exp_out_4_10_reg(2) and exp_out_4_10_reg(3) and exp_out_4_10_reg(4) and exp_out_4_10_reg(5) and exp_out_4_10_reg(6) and exp_out_4_10_reg(7) and exp_out_4_10_reg(8) and exp_out_4_10_reg(9));

  exp_out_4_rndup(0) <= not exp_out_4_reg_0(0);
  exp_out_4_rndup(1) <= not exp_out_4_reg_1(1) when exp_out_4_rndup_1_1_flag = '1' else
                    exp_out_4_reg_2(1);
  exp_out_4_rndup(2) <= not exp_out_4_reg_1(2) when exp_out_4_rndup_2_1_flag = '1' else
                    exp_out_4_reg_2(2);
  exp_out_4_rndup(3) <= not exp_out_4_reg_1(3) when exp_out_4_rndup_3_1_flag = '1' else
                    exp_out_4_reg_2(3);
  exp_out_4_rndup(4) <= not exp_out_4_reg_1(4) when exp_out_4_rndup_4_1_flag = '1' else
                    exp_out_4_reg_2(4);
  exp_out_4_rndup(5) <= not exp_out_4_reg_1(5) when exp_out_4_rndup_5_1_flag = '1' else
                    exp_out_4_reg_2(5);
  exp_out_4_rndup(6) <= not exp_out_4_reg_1(6) when exp_out_4_rndup_6_1_flag = '1' else
                    exp_out_4_reg_2(6);
  exp_out_4_rndup(7) <= not exp_out_4_reg_1(7) when exp_out_4_rndup_7_1_flag = '1' else
                    exp_out_4_reg_2(7);
  exp_out_4_rndup(8) <= not exp_out_4_reg_1(8) when exp_out_4_rndup_8_1_flag = '1' else
                    exp_out_4_reg_2(8);
  exp_out_4_rndup(9) <= not exp_out_4_reg_1(9) when exp_out_4_rndup_9_1_flag = '1' else
                    exp_out_4_reg_2(9);
  exp_out_4_rndup(10) <= not exp_out_4_reg_1(10) when exp_out_4_rndup_10_1_flag = '1' else
                    exp_out_4_reg_2(10);

  save_6: process (clk)
  begin
    if (clk'event and clk = '1')  then
      exp_out_6 <= exp_out_5;
      exp_out_7 <= exp_out_6;
      sine_13 <= sine_12;
      sine_14 <= sine_13;
      temp_buffer_13 <= temp_buffer_12;
      temp_buffer_14 <= temp_buffer_13;
      mant_rndup_reg <= mant_rndup;
      mant_reg <= mant_reg_0;
      roundup_flag_reg <= roundup_flag_reg_0;
      exp_out_4_rndup_reg <= exp_out_4_rndup;
    end if;
  end process save_6;

  --save_result: process (clk)
  --begin
  --  if (clk'event and clk = '1')  then
  --    if temp_buffer_10 /= x"0000000000000000" then
  --      vec_fp <= sine_10 & exp_out_3 & mant;
  --    else
  --      vec_fp <= x"0000000000000000";
  --    end if;
  --  end if;
  --end process save_result;
  
  vec_dp <= x"0000000000000000" when temp_buffer_14 = x"0000000000000000" else
            sine_14 & exp_out_4_rndup_reg & mant_rndup_reg when roundup_flag_reg = '1' else
            sine_14 & exp_out_7 & mant_reg ;  
 
  --sine <= vec_int(63);
  
  --=========================================================================
  --convert out of 2's compliment:
  bit_64_block_next_reg <= vec_int;
  conv_sin_reg_0 <= vec_int(63);


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
      bit_16_block_0_reg <= bit_16_block_0;
      bit_16_block_1_reg <= bit_16_block_1;
      bit_16_block_2_reg <= bit_16_block_2;
      bit_16_block_3_reg <= bit_16_block_3;

      bit_16_block_0_zero_flag_reg <= bit_16_block_0_zero_flag;
      bit_16_block_1_zero_flag_reg <= bit_16_block_1_zero_flag;
      bit_16_block_2_zero_flag_reg <= bit_16_block_2_zero_flag;

      --bit_16_block_0_zero_flag_reg_fan_0 <= bit_16_block_0_zero_flag;
      --bit_16_block_0_zero_flag_reg_fan_1 <= bit_16_block_0_zero_flag;
      --bit_16_block_0_zero_flag_reg_fan_2 <= bit_16_block_0_zero_flag;
      
      --bit_16_block_1_zero_flag_reg_fan_0 <= bit_16_block_1_zero_flag;
      --bit_16_block_1_zero_flag_reg_fan_1 <= bit_16_block_1_zero_flag;
      
      conv_sin_reg_1 <= conv_sin_reg_0;
      conv_sin_reg_1_0 <= conv_sin_reg_0;
      conv_sin_reg_1_1 <= conv_sin_reg_0;
      conv_sin_reg_1_2 <= conv_sin_reg_0;

      bit_16_block_0_zero_flag_reg_1_reg <= bit_16_block_0_zero_flag;
      bit_16_block_1_zero_flag_reg_1_reg <= bit_16_block_1_zero_flag;
      bit_16_block_2_zero_flag_reg_1_reg <= bit_16_block_2_zero_flag;
    end if;
  end process conv_to_non_neg_0;
      
  bit_16_block_1_reg_exor <= exor(bit_16_block_1_reg, conv_sin_reg_1_0);
  bit_16_block_2_reg_exor <= exor(bit_16_block_2_reg, conv_sin_reg_1_1);
  bit_16_block_3_reg_exor <= exor(bit_16_block_3_reg, conv_sin_reg_1_2);

  conv_to_non_neg_0_1: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_reg_1 <= bit_16_block_0_reg;
      bit_16_block_1_reg_1 <= bit_16_block_1_reg;
      bit_16_block_2_reg_1 <= bit_16_block_2_reg;
      bit_16_block_3_reg_1 <= bit_16_block_3_reg;

      bit_16_block_1_reg_exor_reg <= bit_16_block_1_reg_exor;
      bit_16_block_2_reg_exor_reg <= bit_16_block_2_reg_exor;
      bit_16_block_3_reg_exor_reg <= bit_16_block_3_reg_exor;

      --bit_16_block_0_zero_flag_reg_reg <= bit_16_block_0_zero_flag_reg;
      --bit_16_block_1_zero_flag_reg_reg <= bit_16_block_1_zero_flag_reg;
      --bit_16_block_2_zero_flag_reg_reg <= bit_16_block_2_zero_flag_reg;

      bit_16_block_0_zero_flag_reg_fan_0 <= bit_16_block_0_zero_flag_reg;
      bit_16_block_0_zero_flag_reg_fan_1 <= bit_16_block_0_zero_flag_reg;
      bit_16_block_0_zero_flag_reg_fan_2 <= bit_16_block_0_zero_flag_reg;
      
      bit_16_block_1_zero_flag_reg_fan_0 <= bit_16_block_1_zero_flag_reg;
      bit_16_block_1_zero_flag_reg_fan_1 <= bit_16_block_1_zero_flag_reg;
      
      conv_sin_reg_1_reg <= conv_sin_reg_1;

      bit_16_block_0_zero_flag_reg_1 <= bit_16_block_0_zero_flag_reg_1_reg;
      bit_16_block_1_zero_flag_reg_1 <= bit_16_block_1_zero_flag_reg_1_reg;
      bit_16_block_2_zero_flag_reg_1 <= bit_16_block_2_zero_flag_reg_1_reg;
    end if;
  end process conv_to_non_neg_0_1;


  bit_16_block_0_new <= bit_16_block_0_reg_1;
  bit_16_block_1_new <= bit_16_block_1_reg_1 when (bit_16_block_0_zero_flag_reg_fan_0='1') else 
		        bit_16_block_1_reg_exor_reg; 
  bit_16_block_2_new <= bit_16_block_2_reg_1 when ( bit_16_block_0_zero_flag_reg_fan_1 and 
				                    bit_16_block_1_zero_flag_reg_fan_0 ) = '1' else 
		        bit_16_block_2_reg_exor_reg; 
  bit_16_block_3_new <= bit_16_block_3_reg_1 when ( bit_16_block_0_zero_flag_reg_fan_2 and 
						    bit_16_block_1_zero_flag_reg_fan_1 and 
						    bit_16_block_2_zero_flag_reg ) = '1' else 
		        bit_16_block_3_reg_exor_reg; 


  conv_to_non_neg_1_0: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_new_reg_0 <= bit_16_block_0_new;
      bit_16_block_1_new_reg_0 <= bit_16_block_1_new;
      bit_16_block_2_new_reg_0 <= bit_16_block_2_new;
      bit_16_block_3_new_reg_0 <= bit_16_block_3_new;
      
      bit_16_block_0_zero_flag_reg_2_0 <= bit_16_block_0_zero_flag_reg_1;
      bit_16_block_1_zero_flag_reg_2_0 <= bit_16_block_1_zero_flag_reg_1;
      bit_16_block_2_zero_flag_reg_2_0 <= bit_16_block_2_zero_flag_reg_1;
      
      bit_16_block_0_zero_flag_reg_2_0_fan_0 <= bit_16_block_0_zero_flag_reg_1;
      bit_16_block_0_zero_flag_reg_2_0_fan_1 <= bit_16_block_0_zero_flag_reg_1;
      bit_16_block_0_zero_flag_reg_2_0_fan_2 <= bit_16_block_0_zero_flag_reg_1;
      bit_16_block_0_zero_flag_reg_2_0_fan_3 <= bit_16_block_0_zero_flag_reg_1;
      
      bit_16_block_1_zero_flag_reg_2_0_fan_0 <= bit_16_block_1_zero_flag_reg_1;
      bit_16_block_1_zero_flag_reg_2_0_fan_1 <= bit_16_block_1_zero_flag_reg_1;
      bit_16_block_1_zero_flag_reg_2_0_fan_2 <= bit_16_block_1_zero_flag_reg_1;
      bit_16_block_1_zero_flag_reg_2_0_fan_3 <= bit_16_block_1_zero_flag_reg_1;
      
      bit_16_block_2_zero_flag_reg_2_0_fan_0 <= bit_16_block_2_zero_flag_reg_1;
      bit_16_block_2_zero_flag_reg_2_0_fan_1 <= bit_16_block_2_zero_flag_reg_1;
      bit_16_block_2_zero_flag_reg_2_0_fan_2 <= bit_16_block_2_zero_flag_reg_1;
      bit_16_block_2_zero_flag_reg_2_0_fan_3 <= bit_16_block_2_zero_flag_reg_1;
      
      conv_sin_reg_2_0 <= conv_sin_reg_1_reg;
      --bit_16_block_next_reg <= bit_16_block_next;
    end if;
  end process conv_to_non_neg_1_0;

  bit_16_block_next(3 downto 0) <= bit_16_block_0_new_reg_0(3 downto 0) when not (bit_16_block_0_zero_flag_reg_2_0_fan_0='1') else
                		   bit_16_block_1_new_reg_0(3 downto 0) when not (bit_16_block_1_zero_flag_reg_2_0_fan_0='1') else
                		   bit_16_block_2_new_reg_0(3 downto 0) when not (bit_16_block_2_zero_flag_reg_2_0_fan_0='1') else
                		   bit_16_block_3_new_reg_0(3 downto 0);

  bit_16_block_next(7 downto 4) <= bit_16_block_0_new_reg_0(7 downto 4) when not (bit_16_block_0_zero_flag_reg_2_0_fan_1='1') else
                		   bit_16_block_1_new_reg_0(7 downto 4) when not (bit_16_block_1_zero_flag_reg_2_0_fan_1='1') else
                		   bit_16_block_2_new_reg_0(7 downto 4) when not (bit_16_block_2_zero_flag_reg_2_0_fan_1='1') else
                		   bit_16_block_3_new_reg_0(7 downto 4);

  bit_16_block_next(11 downto 8) <= bit_16_block_0_new_reg_0(11 downto 8) when not (bit_16_block_0_zero_flag_reg_2_0_fan_2='1') else
                		    bit_16_block_1_new_reg_0(11 downto 8) when not (bit_16_block_1_zero_flag_reg_2_0_fan_2='1') else
                		    bit_16_block_2_new_reg_0(11 downto 8) when not (bit_16_block_2_zero_flag_reg_2_0_fan_2='1') else
                		    bit_16_block_3_new_reg_0(11 downto 8);

  bit_16_block_next(15 downto 12) <= bit_16_block_0_new_reg_0(15 downto 12) when not (bit_16_block_0_zero_flag_reg_2_0_fan_3='1') else
                		     bit_16_block_1_new_reg_0(15 downto 12) when not (bit_16_block_1_zero_flag_reg_2_0_fan_3='1') else
                		     bit_16_block_2_new_reg_0(15 downto 12) when not (bit_16_block_2_zero_flag_reg_2_0_fan_3='1') else
                		     bit_16_block_3_new_reg_0(15 downto 12);


  conv_to_non_neg_1: process (clk) is
  begin
    if(clk'event and clk = '1')  then
      bit_16_block_0_new_reg_1 <= bit_16_block_0_new_reg_0;
      bit_16_block_1_new_reg_1 <= bit_16_block_1_new_reg_0;
      bit_16_block_2_new_reg_1 <= bit_16_block_2_new_reg_0;
      bit_16_block_3_new_reg_1 <= bit_16_block_3_new_reg_0;

      bit_16_block_0_zero_flag_reg_2 <= bit_16_block_0_zero_flag_reg_2_0;
      bit_16_block_1_zero_flag_reg_2 <= bit_16_block_1_zero_flag_reg_2_0;
      bit_16_block_2_zero_flag_reg_2 <= bit_16_block_2_zero_flag_reg_2_0;

      conv_sin_reg_2 <= conv_sin_reg_2_0;
      bit_16_block_next_reg <= bit_16_block_next;
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
  
  
  bit_16_block_0_final <= bit_4_block_3_final_0 & bit_4_block_2_final_0 & bit_4_block_1_final_0 & bit_4_block_0_final_0 when not (bit_16_block_0_zero_flag_reg_6='1') else
                         bit_16_block_0_new_reg_5;
  bit_16_block_1_final <= bit_4_block_3_final_1 & bit_4_block_2_final_1 & bit_4_block_1_final_1 & bit_4_block_0_final_1 when (bit_16_block_0_zero_flag_reg_6='1') and
                                              not (bit_16_block_1_zero_flag_reg_6='1') else
                         bit_16_block_1_new_reg_5;
  bit_16_block_2_final <= bit_4_block_3_final_2 & bit_4_block_2_final_2 & bit_4_block_1_final_2 & bit_4_block_0_final_2 when (bit_16_block_0_zero_flag_reg_6='1') and
                                              (bit_16_block_1_zero_flag_reg_6='1') and
                                              not (bit_16_block_2_zero_flag_reg_6='1') else
                         bit_16_block_2_new_reg_5;
  bit_16_block_3_final <= bit_4_block_3_final_3 & bit_4_block_2_final_3 & bit_4_block_1_final_3 & bit_4_block_0_final_3 when (bit_16_block_0_zero_flag_reg_6='1') and
                                              (bit_16_block_1_zero_flag_reg_6='1') and
                                              (bit_16_block_2_zero_flag_reg_6='1') else
                         bit_16_block_3_new_reg_5;


  bit_64_block_0_final <= bit_16_block_3_final & bit_16_block_2_final & bit_16_block_1_final & bit_16_block_0_final;
  
  --=========================================================================
  
  
  --temp_buffer <= '0' & vec_int(62 downto 0);
  temp_buffer <= bit_64_block_0_final;
  temp_buffer_cpy <= bit_64_block_0_final;
  exp(10 downto 6) <= b"00000";
  
  find_highest_one_0: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      --temp_int_5_t <= break_apart_t(temp_buffer);
      temp_int_4_3 <= break_apart_3(temp_buffer);
      temp_int_4_2 <= break_apart_2(temp_buffer);
      --temp_int_5_b <= break_apart_b(temp_buffer);
      temp_int_4_1 <= break_apart_1(temp_buffer);
      temp_int_4_0 <= break_apart_0(temp_buffer);
      sine_0 <= conv_sin_reg_6;
      temp_buffer_0 <= temp_buffer_cpy; --just carrying it through 
    end if;
  end process find_highest_one_0;
  
  temp_int_4_3_cond <= temp_int_4_3(15) or temp_int_4_3(14) or temp_int_4_3(13) or temp_int_4_3(12) or
                       temp_int_4_3(11) or temp_int_4_3(10) or temp_int_4_3(9) or temp_int_4_3(8) or
	               temp_int_4_3(7) or temp_int_4_3(6) or temp_int_4_3(5) or temp_int_4_3(4) or
	               temp_int_4_3(3) or temp_int_4_3(2) or temp_int_4_3(1) or temp_int_4_3(0);
  
  temp_int_4_2_cond <= temp_int_4_2(15) or temp_int_4_2(14) or temp_int_4_2(13) or temp_int_4_2(12) or
                       temp_int_4_2(11) or temp_int_4_2(10) or temp_int_4_2(9) or temp_int_4_2(8) or
	               temp_int_4_2(7) or temp_int_4_2(6) or temp_int_4_2(5) or temp_int_4_2(4) or
	               temp_int_4_2(3) or temp_int_4_2(2) or temp_int_4_2(1) or temp_int_4_2(0);
  
  temp_int_4_1_cond <= temp_int_4_1(15) or temp_int_4_1(14) or temp_int_4_1(13) or temp_int_4_1(12) or
                       temp_int_4_1(11) or temp_int_4_1(10) or temp_int_4_1(9) or temp_int_4_1(8) or
	               temp_int_4_1(7) or temp_int_4_1(6) or temp_int_4_1(5) or temp_int_4_1(4) or
	               temp_int_4_1(3) or temp_int_4_1(2) or temp_int_4_1(1) or temp_int_4_1(0);
  
  
  find_highest_one_1_0: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      sine_1_0 <= sine_0;
      temp_buffer_1_0 <= temp_buffer_0;
      temp_int_4_3_cond_reg <= temp_int_4_3_cond;
      temp_int_4_2_cond_reg <= temp_int_4_2_cond;
      temp_int_4_1_cond_reg <= temp_int_4_1_cond;
      temp_int_4_3_reg_0 <= temp_int_4_3;
      temp_int_4_2_reg_0 <= temp_int_4_2;
      temp_int_4_1_reg_0 <= temp_int_4_1;
      temp_int_4_0_reg_0 <= temp_int_4_0;
    end if;
  end process find_highest_one_1_0;
  temp_int_4_3_cond_0 <= temp_int_4_3_cond_reg;
  temp_int_4_3_cond_1 <= temp_int_4_3_cond_reg;
  temp_int_4_3_cond_2 <= temp_int_4_3_cond_reg;
  temp_int_4_3_cond_3 <= temp_int_4_3_cond_reg;
  temp_int_4_3_cond_4 <= temp_int_4_3_cond_reg;
  temp_int_4_3_cond_5 <= temp_int_4_3_cond_reg;
  temp_int_4_2_cond_0 <= temp_int_4_2_cond_reg;
  temp_int_4_2_cond_1 <= temp_int_4_2_cond_reg;
  temp_int_4_2_cond_2 <= temp_int_4_2_cond_reg;
  temp_int_4_2_cond_3 <= temp_int_4_2_cond_reg;
  temp_int_4_2_cond_4 <= temp_int_4_2_cond_reg;
  temp_int_4_2_cond_5 <= temp_int_4_2_cond_reg;
  temp_int_4_1_cond_0 <= temp_int_4_1_cond_reg;
  temp_int_4_1_cond_1 <= temp_int_4_1_cond_reg;
  temp_int_4_1_cond_2 <= temp_int_4_1_cond_reg;
  temp_int_4_1_cond_3 <= temp_int_4_1_cond_reg;
  temp_int_4_1_cond_4 <= temp_int_4_1_cond_reg;
  temp_int_4_1_cond_5 <= temp_int_4_1_cond_reg;
  find_highest_one_1_1: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      sine_1_1 <= sine_1_0;
      temp_buffer_1_1 <= temp_buffer_1_0;
      --temp_int_4_3_cond_reg <= temp_int_4_3_cond;
      --temp_int_4_2_cond_reg <= temp_int_4_2_cond;
      --temp_int_4_1_cond_reg <= temp_int_4_1_cond;
      temp_int_4_3_cond_reg_0 <= temp_int_4_3_cond_0;
      temp_int_4_3_cond_reg_1 <= temp_int_4_3_cond_1;
      temp_int_4_3_cond_reg_2 <= temp_int_4_3_cond_2;
      temp_int_4_3_cond_reg_3 <= temp_int_4_3_cond_3;
      temp_int_4_3_cond_reg_4 <= temp_int_4_3_cond_4;
      temp_int_4_3_cond_reg_5 <= temp_int_4_3_cond_5;
      temp_int_4_2_cond_reg_0 <= temp_int_4_2_cond_0;
      temp_int_4_2_cond_reg_1 <= temp_int_4_2_cond_1;
      temp_int_4_2_cond_reg_2 <= temp_int_4_2_cond_2;
      temp_int_4_2_cond_reg_3 <= temp_int_4_2_cond_3;
      temp_int_4_2_cond_reg_4 <= temp_int_4_2_cond_4;
      temp_int_4_2_cond_reg_5 <= temp_int_4_2_cond_5;
      temp_int_4_1_cond_reg_0 <= temp_int_4_1_cond_0;
      temp_int_4_1_cond_reg_1 <= temp_int_4_1_cond_1;
      temp_int_4_1_cond_reg_2 <= temp_int_4_1_cond_2;
      temp_int_4_1_cond_reg_3 <= temp_int_4_1_cond_3;
      temp_int_4_1_cond_reg_4 <= temp_int_4_1_cond_4;
      temp_int_4_1_cond_reg_5 <= temp_int_4_1_cond_5;
      temp_int_4_3_reg <= temp_int_4_3_reg_0;
      temp_int_4_2_reg <= temp_int_4_2_reg_0;
      temp_int_4_1_reg <= temp_int_4_1_reg_0;
      temp_int_4_0_reg <= temp_int_4_0_reg_0;
    end if;
  end process find_highest_one_1_1;
  
  temp_int_2_3_wire <= break_apart_3(temp_int_4_3_reg) when temp_int_4_3_cond_reg_0 = '1' else
                       break_apart_3(temp_int_4_2_reg) when temp_int_4_2_cond_reg_0 = '1' else
                       break_apart_3(temp_int_4_1_reg) when temp_int_4_1_cond_reg_0 = '1' else
                       break_apart_3(temp_int_4_0_reg);
  temp_int_2_2_wire <= break_apart_2(temp_int_4_3_reg) when temp_int_4_3_cond_reg_1 = '1' else
                       break_apart_2(temp_int_4_2_reg) when temp_int_4_2_cond_reg_1 = '1' else
                       break_apart_2(temp_int_4_1_reg) when temp_int_4_1_cond_reg_1 = '1' else
                       break_apart_2(temp_int_4_0_reg);
  temp_int_2_1_wire <= break_apart_1(temp_int_4_3_reg) when temp_int_4_3_cond_reg_2 = '1' else
                       break_apart_1(temp_int_4_2_reg) when temp_int_4_2_cond_reg_2 = '1' else
                       break_apart_1(temp_int_4_1_reg) when temp_int_4_1_cond_reg_2 = '1' else
                       break_apart_1(temp_int_4_0_reg);
  temp_int_2_0_wire <= break_apart_0(temp_int_4_3_reg) when temp_int_4_3_cond_reg_3 = '1' else
                       break_apart_0(temp_int_4_2_reg) when temp_int_4_2_cond_reg_3 = '1' else
                       break_apart_0(temp_int_4_1_reg) when temp_int_4_1_cond_reg_3 = '1' else
                       break_apart_0(temp_int_4_0_reg);
  shift_cnt_wire <= b"00" when temp_int_4_3_cond_reg_4 = '1' else
                    b"01" when temp_int_4_2_cond_reg_4 = '1' else
                    b"10" when temp_int_4_1_cond_reg_4 = '1' else
                    b"11";
  exp_wire <= b"11" when temp_int_4_3_cond_reg_5 = '1' else
              b"10" when temp_int_4_2_cond_reg_5 = '1' else
              b"01" when temp_int_4_1_cond_reg_5 = '1' else
              b"00";
  
  find_highest_one_1: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      sine_1 <= sine_1_1;
      temp_buffer_1 <= temp_buffer_1_1;
      temp_int_2_3 <= temp_int_2_3_wire;
      temp_int_2_2 <= temp_int_2_2_wire;
      temp_int_2_1 <= temp_int_2_1_wire;
      temp_int_2_0 <= temp_int_2_0_wire;
      shift_cnt(5 downto 4) <= shift_cnt_wire;
      exp(5 downto 4) <= exp_wire;
    end if;
  end process find_highest_one_1;
  
  temp_int_2_3_cond <= temp_int_2_3(3) or temp_int_2_3(2) or temp_int_2_3(1) or temp_int_2_3(0);
  temp_int_2_2_cond <= temp_int_2_2(3) or temp_int_2_2(2) or temp_int_2_2(1) or temp_int_2_2(0);
  temp_int_2_1_cond <= temp_int_2_1(3) or temp_int_2_1(2) or temp_int_2_1(1) or temp_int_2_1(0);
  
  find_highest_one_2_0: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      sine_2_0 <= sine_1;
      temp_buffer_2_0 <= temp_buffer_1;
      shift_cnt_2_0(5 downto 4) <= shift_cnt(5 downto 4);
      shift_cnt_2_0(1 downto 0) <= shift_cnt(1 downto 0);
      exp_2_0(10 downto 4) <= exp(10 downto 4);
      exp_2_0(1 downto 0) <= exp(1 downto 0);
      temp_int_2_3_reg <= temp_int_2_3;
      temp_int_2_2_reg <= temp_int_2_2;
      temp_int_2_1_reg <= temp_int_2_1;
      temp_int_2_0_reg <= temp_int_2_0;
      temp_int_2_3_cond_reg <= temp_int_2_3_cond;
      temp_int_2_2_cond_reg <= temp_int_2_2_cond;
      temp_int_2_1_cond_reg <= temp_int_2_1_cond;
    end if;
  end process find_highest_one_2_0;
  
  temp_int_0_3_wire <= break_apart_3_lg(temp_int_2_3_reg) when temp_int_2_3_cond_reg = '1' else
                       break_apart_3_lg(temp_int_2_2_reg) when temp_int_2_2_cond_reg = '1' else
                       break_apart_3_lg(temp_int_2_1_reg) when temp_int_2_1_cond_reg = '1' else
                       break_apart_3_lg(temp_int_2_0_reg);
  temp_int_0_2_wire <= break_apart_2_lg(temp_int_2_3_reg) when temp_int_2_3_cond_reg = '1' else
                       break_apart_2_lg(temp_int_2_2_reg) when temp_int_2_2_cond_reg = '1' else
                       break_apart_2_lg(temp_int_2_1_reg) when temp_int_2_1_cond_reg = '1' else
                       break_apart_2_lg(temp_int_2_0_reg);
  temp_int_0_1_wire <= break_apart_1_lg(temp_int_2_3_reg) when temp_int_2_3_cond_reg = '1' else
                       break_apart_1_lg(temp_int_2_2_reg) when temp_int_2_2_cond_reg = '1' else
                       break_apart_1_lg(temp_int_2_1_reg) when temp_int_2_1_cond_reg = '1' else
                       break_apart_1_lg(temp_int_2_0_reg);
  temp_int_0_0_wire <= break_apart_0_lg(temp_int_2_3_reg) when temp_int_2_3_cond_reg = '1' else
                       break_apart_0_lg(temp_int_2_2_reg) when temp_int_2_2_cond_reg = '1' else
                       break_apart_0_lg(temp_int_2_1_reg) when temp_int_2_1_cond_reg = '1' else
                       break_apart_0_lg(temp_int_2_0_reg);
  
  shift_cnt_2_wire <= b"00" when temp_int_2_3_cond_reg = '1' else
                      b"01" when temp_int_2_2_cond_reg = '1' else
                      b"10" when temp_int_2_1_cond_reg = '1' else
                      b"11";
  exp_2_wire <= b"11" when temp_int_2_3_cond_reg = '1' else
                b"10" when temp_int_2_2_cond_reg = '1' else
                b"01" when temp_int_2_1_cond_reg = '1' else
                b"00";
  
  find_highest_one_2: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      sine_2 <= sine_2_0;
      temp_buffer_2 <= temp_buffer_2_0;
      shift_cnt_2(5 downto 4) <= shift_cnt_2_0(5 downto 4);
      shift_cnt_2(1 downto 0) <= shift_cnt_2_0(1 downto 0);
      exp_2(10 downto 4) <= exp_2_0(10 downto 4);
      exp_2(1 downto 0) <= exp_2_0(1 downto 0);
      
      temp_int_0_3 <= temp_int_0_3_wire;
      temp_int_0_2 <= temp_int_0_2_wire;
      temp_int_0_1 <= temp_int_0_1_wire;
      temp_int_0_0 <= temp_int_0_0_wire;
      shift_cnt_2(3 downto 2) <= shift_cnt_2_wire;
      exp_2(3 downto 2) <= exp_2_wire;
    end if;
  end process find_highest_one_2;
  
  shift_cnt_3_wire <= b"00" when temp_int_0_3 /= '0' else
                      b"01" when temp_int_0_2 /= '0' else
                      b"10" when temp_int_0_1 /= '0' else
                      b"11";
  exp_3_wire <= b"11" when temp_int_0_3 /= '0' else
                b"10" when temp_int_0_2 /= '0' else
                b"01" when temp_int_0_1 /= '0' else
                b"00";
  
  find_highest_one_3: process (clk) is
  begin
    if (clk'event and clk = '1')  then
      sine_3 <= sine_2;
      temp_buffer_3 <= temp_buffer_2;
      shift_cnt_3(5 downto 2) <= shift_cnt_2(5 downto 2);
      exp_3(10 downto 2) <= exp_2(10 downto 2);
      shift_cnt_3(1 downto 0) <= shift_cnt_3_wire;
      exp_3(1 downto 0) <= exp_3_wire;
    end if;
  end process find_highest_one_3;
end architecture behavioural;


