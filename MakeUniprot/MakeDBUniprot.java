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
  run on uniprot_sprot.dat.gz and uniprot_trembl.dat.gz
  released with a given version of Pfam
*/
public class MakeDBUniprot {
    final public static SimpleDateFormat uniprotDateFormat =
        new SimpleDateFormat ("dd-MMM-yyyy");
    
    final public static void main(String argv[]) {
        try {
            LocalSQL.connectRW();
            PreparedStatement stmt1 = LocalSQL.prepareStatement("select id from uniprot where long_id=? and is_swissprot=? and seq_version=?");
            PreparedStatement stmt2 = LocalSQL.prepareStatement("insert into uniprot values (null, ?, ?, ?, ?, ?, 0)",
                                                                Statement.RETURN_GENERATED_KEYS);
            PreparedStatement stmt3 = LocalSQL.prepareStatement("insert into uniprot_accession values (?, ?)");
            PreparedStatement stmt4 = LocalSQL.prepareStatement("insert into uniprot_pfam values (?, ?)");
            PreparedStatement stmt5 = LocalSQL.prepareStatement("update uniprot set is_obsolete=0 where id=?");
            Statement stmt = LocalSQL.createStatement();
	    
            ResultSet rs;

            // skip lines?
            long skipLines = 0;
            if (argv.length > 1)
                skipLines = StringUtil.atol(argv[1]);

            if (skipLines > 0)
                System.out.println("Skipping first "+skipLines+" records of file");

            // get version from cmd line
            int pfamReleaseID = LocalSQL.lookupPfamRelease(argv[0]);
            if (pfamReleaseID==0)
                throw new Exception("no such Pfam version "+argv[0]);

            boolean isSprot = (argv[1].indexOf("sprot") > -1);

            if (skipLines==0)
                stmt.executeUpdate("update uniprot set is_obsolete=1 where is_swissprot="+(isSprot ? 1 : 0));

            // add all sequences in file
            BufferedReader infile = IO.openReader(argv[1]);
            String longID= null, name=null, seq="";
            int seqVer = 0;
            java.util.Date seqDate = null;
            String[] accs = null;
            Vector<Integer> pfams = new Vector<Integer>();
		
            while (infile.ready()) {
                String buffer = infile.readLine().trim();

                if (buffer.startsWith("ID ")) {
                    if (skipLines-- <= 0) {
                        longID = buffer.substring(5);
                        int pos = longID.indexOf(' ');
                        longID = longID.substring(0,pos);
                    }
                }
                else if ((buffer.startsWith("DT ")) &&
                         (buffer.indexOf("sequence version")==18)) {
                    if (skipLines <= 0) {
                        seqVer = StringUtil.atoi(buffer,35);
                        seqDate = uniprotDateFormat.parse(buffer.substring(5,16));
                    }
                }
                else if ((buffer.startsWith("DE ")) &&
                         ((buffer.indexOf("RecName: Full=")==5) ||
                          (buffer.indexOf("SubName: Full=")==5))) {
                    if (skipLines <= 0) {
                        name = buffer.substring(19,buffer.length()-1);
                    }
                }
                else if (buffer.startsWith("AC ")) {
                    if (skipLines <= 0) {
                        accs = buffer.substring(5).split(";");
                    }
                }
                else if ((buffer.startsWith("DR ")) &&
                         (buffer.indexOf("Pfam; ")==5)) {
                    if (skipLines <= 0) {
                        String pfamAcc = buffer.substring(11);
                        int pos = pfamAcc.indexOf(';');
                        pfamAcc = pfamAcc.substring(0,pos);
                        int pfamID = LocalSQL.lookupPfam(pfamAcc,
                                                         pfamReleaseID);
                        if (pfamID > 0)
                            pfams.add(new Integer(pfamID));
                    }
                }
                else if (buffer.startsWith("SQ ")) {
                    while (!buffer.startsWith("//")) {
                        buffer = infile.readLine().trim();
                        // if (!buffer.startsWith("//"))
                        // seq += StringUtil.replace(buffer," ","").toLowerCase();
                    }

                    /*
                      System.out.println("id = '"+longID+"'");
                      System.out.println("seqVer = '"+seqVer+"'");
                      System.out.println("seqDate = '"+ParsePDB.sqlDateFormat.format(seqDate)+"'");
                      System.out.println("name = '"+name+"'");
                      for (int i=0; i<accs.length; i++)
                      System.out.println("acc = '"+accs[i].trim()+"'");
                      for (int i=0; i<pfams.size(); i++)
                      System.out.println("pfam = '"+pfams.get(i)+"'");
                      System.out.println("seq = '"+seq+"'");
                    */

                    // int seqID = MakeDomainSeq.lookupOrCreateSeq(seq);

                    if (skipLines <= 0) {
                        stmt1.setString(1,longID);
                        stmt2.setString(1,longID);
                        stmt1.setInt(2,isSprot?1:0);
                        stmt2.setInt(2,isSprot?1:0);
                        stmt1.setInt(3,seqVer);
                        stmt2.setInt(3,seqVer);
                        stmt2.setDate(4,new java.sql.Date(seqDate.getTime()));
                        stmt2.setString(5,name);
                        // stmt2.setInt(6,seqID);

                        int id = 0;
                        // find old sequence
                        rs = stmt1.executeQuery();
                        if (rs.next()) {
                            id = rs.getInt(1);
                        }
                        rs.close();
                        if (id==0) {
                            // create new sequence
                            stmt2.executeUpdate();
                            rs = stmt2.getGeneratedKeys();
                            rs.next();
                            id = rs.getInt(1);
                            rs.close();
                        }
                        else {
                            // un-obsolete it
                            stmt5.setInt(1,id);
                            stmt5.executeUpdate();
                        }

                        // add accessions
                        stmt.executeUpdate("delete from uniprot_accession where uniprot_id="+id);
                        stmt3.setInt(1,id);
                        for (int i=0; i<accs.length; i++) {
                            stmt3.setString(2,accs[i].trim());
                            stmt3.executeUpdate();
                        }

                        // add pfams
                        stmt.executeUpdate("delete from uniprot_pfam where uniprot_id="+id);
                        stmt4.setInt(1,id);
                        for (int i=0; i<pfams.size(); i++) {
                            stmt4.setInt(2,pfams.get(i).intValue());
                            stmt4.executeUpdate();
                        }

                        longID= null;
                        name=null;
                        seq="";
                        seqVer = 0;
                        seqDate = null;
                        accs = null;
                        pfams = new Vector<Integer>();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
