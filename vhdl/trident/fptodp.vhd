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

entity fp_to_dp is
  port(vec_fp: in std_logic_vector(31 downto 0);
       vec_dp:out std_logic_vector(63 downto 0);
       clk: in std_logic);
end entity fp_to_dp;

architecture behavioural of fp_to_dp is
    signal mant: std_logic_vector(51 downto 0);
    signal sine: std_logic;
    signal exp: std_logic_vector(10 downto 0);
begin
  mant <= vec_fp(22 downto 0) & x"0000000" & '0';
  sine <= vec_fp(31);
  exp <= x"7" & vec_fp(29 downto 23) when vec_fp(30) = '0' else
         x"8" & vec_fp(29 downto 23);

  save_result: process (clk)
  begin
    if (clk'event and clk = '1') then
      if vec_fp(30 downto 23) = x"ff" then --and 
         --vec_fp(22 downto 0) /= b"00000000000000000000000" then 
        vec_dp <= sine & b"111" & x"ff" & mant;
      --elsif vec_fp(30 downto 23) = x"ff" and 
      --      vec_fp(22 downto 0) = b"00000000000000000000000" and
      --      vec_fp(31) = 1 then
      elsif vec_fp = x"00000000" then
          vec_dp <= x"0000000000000000";
      else
        vec_dp <= sine & exp & mant;
      end if;
    end if;
  end process save_result;


end architecture behavioural;
