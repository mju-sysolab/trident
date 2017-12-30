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



library IEEE;
use IEEE.STD_LOGIC_1164.all;

package TridentComps is
	
  -- Floating Point Absolute Value
  component fpAbs is
    generic ( ew : integer := 8;  	-- exponent width  
              mw : integer := 24 	-- mantissa width
            );
    port ( a : in std_logic_vector (mw+ew-1 downto 0);  -- input
           q : out std_logic_vector (mw+ew-1 downto 0)  -- inverted output
         );		
  end component fpAbs;

  -- Floating Point Inverter
  component fpInv is
    generic ( ew : integer := 8;  	-- exponent width  
              mw : integer := 24 	-- mantissa width
            );  
    port ( a : in std_logic_vector (mw+ew-1 downto 0);  -- input
           q : out std_logic_vector (mw+ew-1 downto 0)  -- inverted output
         );		
  end component fpInv;

  --integer to floating point cast
  component i_to_fp is
    port ( vec_int : in std_logic_vector(31 downto 0);
           vec_fp  : out std_logic_vector(31 downto 0);
           clk     : in std_logic);
  end component i_to_fp;

  -- integer to double cast
  component i_to_dp is
    port ( vec_int : in std_logic_vector(63 downto 0);
	   vec_dp  : out std_logic_vector(63 downto 0);
	   clk     : in std_logic);
  end component i_to_dp;

  --floating point to double cast
  component fp_to_dp is
    port ( vec_fp  : in std_logic_vector(31 downto 0);
	   vec_dp  : out std_logic_vector(63 downto 0);
	   clk     : in std_logic);
  end component fp_to_dp;

  --double to floating point cast
  component dp_to_fp is
    port ( vec_fp : out std_logic_vector(31 downto 0);
	   vec_dp : in std_logic_vector(63 downto 0);
	   clk    : in std_logic);
  end component dp_to_fp;

  --floating point to integer
  component fp_to_i is
    port ( vec_int : out std_logic_vector(31 downto 0);
	   vec_fp  : in std_logic_vector(31 downto 0);
	   clk     : in std_logic);
  end component fp_to_i;

  --double to integer cast
  component dp_to_i is
    port ( vec_int : out std_logic_vector(63 downto 0);
	   vec_dp  : in std_logic_vector(63 downto 0);
	   clk     : in std_logic);
  end component dp_to_i;
end package;

--end package body;

