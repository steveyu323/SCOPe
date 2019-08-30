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
The function performs the following:
1) select the release_id with raf version = 18
2) for each of the release_id, identify the pdb_chain_id relating to it that is linked to the raf_version = 3
3) get the dbref entry of the above pdb_chain_id
4) get the alignment information from the pdb_chain_db ref, and substring using pdb_chain_dbref align_start and align_end
5) get the uniprot sequence from astral chain and the seq from raf entry
6) perform string comparison
7) Store in a new table compare_seq with columns:
(pdb_release_id INT NOT NULL,
pdb_chain_id INT NOT NULL,
raf_available Bool NOT NULL,
long_id TEXT NOT NULL,
uniprot_id INT NOT NULL,
seq_id INT NOT NULL,
is_same Bool NOT NULL)
*/

public class CompareUniprotSeqres {
  final public static void compareUniSeqRes(int pdbReleaseID) throws Exception {
      Statement stmt = LocalSQL.createStatement();
      PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id = ? and is_obsolete = 0")
      PreparedStatement stmt2 = LocalSQL.prepareStatement("select seq_id from uniprot_seq where uniprot_id = ?")
      PreparedStatement stmt3 = LocalSQL.prepareStatement("select seq from astral_seq where id = ? and is_reject = 0")

      PreparedStatement stmt4 = LocalSQL.prepareStatement("insert into pdb_chain_dbref values (null, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                 Statement.RETURN_GENERATED_KEYS);
      PreparedStatement stmt5 = LocalSQL.prepareStatement("insert into pdb_chain_diff values (null, ?, ?, ?, ?, ?, ?)");

      ResultSet rs;
      ResultSet rs1;
      ResultSet rs2;

      rs = stmt.executeQuery("select * from pdb_chain_dbref where db_name = 'UNP' and pdb_chain_id in (select pdb_chain_id from raf where pdb_chain_id in (select id from pdb_chain where pdb_release_id ="+pdbReleaseID + ") and raf_version_id = 3)");

      while (rs.next()) {
          int pdb_chain_id = rs.getInt(2);
          String db_code = rs.getString(4);
          int db_align_start = rs.getInt(6);
          int db_align_end = rs.getInt(7);
          int pdb_align_start = rs.getInt(8);
          int pdb_align_end = rs.getInt(9);



          stmt1.close();
          stmt2.close();
          stmt3.close();
      }




      stmt.close();

  }



  final public static void main(String argv[]) {
    try {
      LocalSQL.connectRW();
      Statement stmt = LocalSQL.createStatement();

      ResultSet rs;
      rs = stmt.executeQuery("select pdb_release_id from pdb_local where xml_path is not null and pdb_release_id in (select distinct(pdb_release_id) from pdb_chain where id in (select pdb_chain_id from raf where last_release_id = 18))");
      while (rs.next()) {
          int id = rs.getInt(1);
          compareUniSeqRes(id);
      }
    } catch (Exception e) {
      System.out.println("Exception: "+e.getMessage());
      e.printStackTrace();
    }
  }

}
