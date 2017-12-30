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


library ieee;

use ieee.std_logic_unsigned.all;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;

entity dp_to_fp is
  port(vec_fp: out std_logic_vector(31 downto 0);
       vec_dp: in std_logic_vector(63 downto 0);
       clk: in std_logic);
end entity dp_to_fp;

architecture behavioural of dp_to_fp is
    signal mant_dp: std_logic_vector(51 downto 0);
    signal mant_fp: std_logic_vector(22 downto 0);
    signal sine: std_logic;
    signal exp_fp: std_logic_vector(7 downto 0);
    signal exp_dp: std_logic_vector(10 downto 0);
begin

  sine <= vec_dp(63);
  mant_dp <= vec_dp(51 downto 0);
  exp_dp <= vec_dp(62 downto 52);
  
  exp_fp <= not exp_dp(7) & exp_dp(6 downto 0);
  mant_fp <= mant_dp(51 downto 29);
  
  save_result: process (clk)
  begin
    if (clk'event and clk = '1') then
      --pass through NaN
      if exp_dp = b"11111111111" and 
         mant_dp /= b"0000000000000000000000000000000000000000000000000000" then 
        vec_fp <= sine & x"ff" & x"fffff" & b"111";
	--check for -inf (this has to be checked before +Inf and overflow or it will think it is overflow!)
      elsif (exp_dp = b"11111111111" and 
            mant_dp = b"0000000000000000000000000000000000000000000000000000" and
            sine = '1') then
        vec_fp <= '1' & x"ff" & x"00000" & b"000";
      --check if inf or overflow and set fp to inf
      elsif ((exp_dp = b"11111111111" and 
            mant_dp = b"0000000000000000000000000000000000000000000000000000" and
            sine = '0') or
	    ((exp_dp(10) = '1') and (exp_dp(9) = '1' or exp_dp(8) = '1')))then
        vec_fp <= '0' & x"ff" & x"00000" & b"000";
      --check for 0 or underfow
      elsif ((vec_dp = x"0000000000000000") or
             ((exp_dp(10) = '0') and (exp_dp(9) = '0' or exp_dp(8) = '0'))) then
        vec_fp <= x"00000000";
      --otherwise create like normal:
      else
        vec_fp <= sine & exp_fp & mant_fp;
      end if;
    end if;
  end process save_result;
  

end architecture behavioural;
