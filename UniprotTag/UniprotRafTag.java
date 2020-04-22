package gov.lbl.scop.app;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.*;
import org.strbio.IO;
import org.strbio.io.*;
import org.strbio.math.*;
import org.strbio.mol.*;
import org.strbio.util.*;
import gov.lbl.scop.local.LocalSQL;
import gov.lbl.scop.util.RAF;

/*
The function performs the following:
- for the new function on extracting the tag from uniprot (as well as checking pdbseqadv)
- for each of the misaligned length in the table
- see whether the tag is already in pdb_chain_diff,
- see if the tag is in pdb_chain_tag
- if not, make it a unprot tag.

- or should we instead do based on pdb_chain_diff and if the diff entry satisfy the new tag definition make it a tag?
*/

public class UniprotRafTag {
  final public static void FindRafTag(int pdbChainID) throws Exception {
    System.out.println("current pdbChainID is " + pdbChainID);
    Statement stmt = LocalSQL.createStatement();
    PreparedStatement stmt1 = LocalSQL.prepareStatement("select * from pdb_chain_dbref where pdb_chain_id = ? and db_name = 'UNP'");
    PreparedStatement stmt2 = LocalSQL.prepareStatement("select raf_get_body(id) from raf where pdb_chain_id = ? and first_release_id is null and last_release_id is null and raf_version_id = 3");


    // PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id = ? and is_obsolete = 0");
    // PreparedStatement stmt2 = LocalSQL.prepareStatement("select seq_id from uniprot_seq where uniprot_id = ?");
    // PreparedStatement stmt3 = LocalSQL.prepareStatement("select seq from astral_seq where id = ? and is_reject = 0");
    // PreparedStatement stmt4 = LocalSQL.prepareStatement("insert into compare_seq values (?,?,?,?,?,?,?)");
    // PreparedStatement stmt5 = LocalSQL.prepareStatement("select raf_get_body(id) from raf where pdb_chain_id = ? and first_release_id is null and last_release_id is null and raf_version_id = 3");

    ResultSet rs1;
    ResultSet rs2;
    // ResultSet rs3;
    // ResultSet rs4;
    // ResultSet rs5;

    stmt1.setInt(1,pdbChainID);
    rs1 = stmt1.executeQuery();
    if (rs1.next()) {
      System.out.println("rs1 is not empty");
      int dbrefID = rs1.getInt(1);
      int pdb_align_start = rs1.getInt(8);
      int pdb_align_end = rs1.getInt(9);
      System.out.println("pdbChainID: " + pdbChainID + "  pdb_align_start: " + pdb_align_start + "  pdb_align_end: " + pdb_align_end);
    }
    rs1.close();

    stmt2.setInt(1,pdbChainID);
    rs2 = stmt2.executeQuery();
    if (rs2.next()) {
      System.out.println("rs2 is not empty");
      raf_sequence = rs5.getString(1);
      System.out.println("raf_sequence = " + raf_sequence);
    }

    stmt1.close();
    stmt2.close();
    stmt.close();
  }



  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();

      ResultSet rs;
      if (argv.length==0) {
        rs = stmt.executeQuery("select distinct(r.pdb_chain_id) from raf as r, scop_uniprot_align_191007 as a where a.pdb_chain_id = r.pdb_chain_id and r.raf_version_id = 3");
        while (rs.next()) {
          int pdbChainID = rs.getInt(1);
          FindRafTag(pdbChainID);
        }
      }
      else {
        FindRafTag(Integer.parseInt(argv[0]));
      }

    } catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }
}
