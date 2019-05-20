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
public class MakeDBUniprot4 {
  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();
      PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id=? and is_obsolete=0");
      PreparedStatement stmt2 = LocalSQL.prepareStatement("insert into missing_uniprot values (?, ?)");
      PreparedStatement stmt3 = LocalSQL.prepareStatement("select seq_id from uniprot_seq where uniprot_id=?");
      ResultSet rs1;
      ResultSet rs2;
      ResultSet rs3;
      int missed_type;
      //type == 0 ---> in both uniprot seq and uniprot table
      //type == 1 ---> not in the uniprot table
      //type == 2 ---> not in the uniprot_seq mapping but in uniprot table
      rs1 = stmt.executeQuery("select distinct(db_code) from pdb_chain_dbref where db_name = 'UNP'");
      while (rs1.next()) {
        String acc = rs1.getString(1);
        System.out.println("long_id = " + acc);
        stmt1.setString(1,acc);
        rs2 = stmt1.executeQuery();
        if (rs2.next()) {
          int uniprotID = rs2.getInt(1);
          stmt3.setInt(1,uniprotID);
          rs3 = stmt3.executeQuery();
          if (rs3.next()) {
            missed_type = 0;
            stmt2.setString(1,acc);
            stmt2.setInt(2,missed_type);
            stmt2.executeUpdate();
          } else {
            missed_type = 2;
            stmt2.setString(1,acc);
            stmt2.setInt(2,missed_type);
            stmt2.executeUpdate();
          }

        } else {
          // The protein long id is not in the current uniprot table
          missed_type = 1;
          stmt2.setString(1,acc);
          stmt2.setInt(2,missed_type);
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
