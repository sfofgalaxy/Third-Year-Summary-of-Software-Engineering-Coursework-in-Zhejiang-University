import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class LuceneTest {
	public static void main(String[] args) {
		LuceneTest w=new LuceneTest();
		String filePath="d:/index";//���������Ĵ洢Ŀ¼
		w.createIndex(filePath);//��������
		w.searrh(filePath);//����
	}
	
	public void createIndex(String filePath){
		File f=new File(filePath);
		IndexWriter iwr=null;
		try {
			Directory dir=FSDirectory.open(f);
			Analyzer analyzer = new IKAnalyzer();
			
//			String contents="����";
	//		printTerms(contents);
			
			IndexWriterConfig conf=new IndexWriterConfig(Version.LUCENE_4_10_0,analyzer);
			iwr=new IndexWriter(dir,conf);//����IndexWriter���̶���·
			Document doc=getDocument();
			iwr.addDocument(doc);//���doc��Lucene�ļ�������documentΪ������λ
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			iwr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Document getDocument(){
		//doc��������field���ɣ��ڼ��������У�Lucene�ᰴ��ָ����Field��������ÿ��document�ĸ���field�Ƿ����Ҫ��
		Document doc=new Document();
		Field f1=new TextField("name","XXX",Field.Store.YES);
		Field f2=new TextField("pic","�����һ������",Field.Store.YES);
		Field f3=new TextField("grade","Ǯѧɭ��ѧϰ�ɼ�ʮ������",Field.Store.YES);
		Field f4=new TextField("address","�㽭��ѧ��ȪУ��",Field.Store.YES);
		Field f5=new StringField("id","��ѧ��",Field.Store.YES);
		doc.add(f1);
		doc.add(f2);
		doc.add(f3);
		doc.add(f4);
		doc.add(f5);
		
		return doc;
		
	}
	
	public void searrh(String filePath){
		File f=new File(filePath);
		try {
			IndexSearcher searcher=new IndexSearcher(DirectoryReader.open(FSDirectory.open(f)));
			String queryStr="XXX";
			Analyzer analyzer = new IKAnalyzer();
			//ָ��fieldΪ��name����Lucene�ᰴ�չؼ�������ÿ��doc�е�name��
			QueryParser parser = new QueryParser(Version.LUCENE_4_10_0, "name", analyzer);
			
			Query query=parser.parse(queryStr);
			TopDocs hits=searcher.search(query,1);//ǰ�漸�д���Ҳ�ǹ̶���·��ʹ��ʱֱ�Ӹ�field�͹ؼ��ʼ���
			for(ScoreDoc doc:hits.scoreDocs){
				Document d=searcher.doc(doc.doc);
				System.out.println(d.get("address"));
				System.out.println(d.get("id"));
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 

}
