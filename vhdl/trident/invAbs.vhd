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


--*****************************************************************************
-- $Header: /Software/VHDL/Lib/fp2/src/build/qxFpTemplate.vhd 1     27/04/04 17:23 Fisher $
--*****************************************************************************
-- Task: qxFP Components package
-- For history information, consult the VSS database
-- Copyright LANL 2005
-- extra stuff we wrote
--*****************************************************************************
library IEEE;
use IEEE.STD_LOGIC_1164.all;

entity fpInv is
	generic (
		ew 			: integer := 8;									-- exponent width  
		mw 			: integer := 24									-- mantissa width
		);
	port (
		a 		: in std_logic_vector (mw+ew-1 downto 0);  -- input
		q 		: out std_logic_vector (mw+ew-1 downto 0)  -- inverted output
        );		
end entity fpInv;

architecture netlist of fpInv is
begin
   
   q<= (not a(a'high)) & a(a'high - 1 downto 0);

end architecture;

library IEEE;
use IEEE.STD_LOGIC_1164.all;

entity fpAbs is
	generic (
		ew 			: integer := 8;									-- exponent width  
		mw 			: integer := 24									-- mantissa width
		);
	port (
		a 		: in std_logic_vector (mw+ew-1 downto 0);  -- input
		q 		: out std_logic_vector (mw+ew-1 downto 0)  -- inverted output
        );		
end entity fpAbs;

architecture netlist of fpAbs is
begin
   
   q<= '0' & a(a'high - 1 downto 0);

end architecture;
