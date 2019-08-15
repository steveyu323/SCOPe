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

/*
run on pfamseq.gz
released with a given version of Pfam
*/
public class ResolveInvalidUniprot1 {
  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();
      PreparedStatement stmt1 = LocalSQL.prepareStatement("select distinct(db_accession) from pdb_chain_dbref_scop where db_code = ?");
      PreparedStatement stmt2 = LocalSQL.prepareStatement("insert into missing_acc values (?, ?,0)");
      ResultSet rs1;
      ResultSet rs2;
      //type == 0 ---> in both uniprot seq and uniprot table
      //type == 1 ---> not in the uniprot table
      //type == 2 ---> not in the uniprot_seq mapping but in uniprot table
      rs1 = stmt.executeQuery("select long_id from missing_uniprot where miss_type = 1");
      while (rs1.next()) {
        String missed_id = rs1.getString(1);
        System.out.println("long_id = " + missed_id);
        stmt1.setString(1,missed_id);
        rs2 = stmt1.executeQuery();
        while (rs2.next()) {
          String missed_acc = rs2.getString(1);
          stmt2.setString(1,missed_acc);
          stmt2.setString(2,missed_id);
          stmt2.executeUpdate();
        }
      }
    }
    catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }
}
