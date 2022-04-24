package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.*;

import java.io.*;
import java.util.*;

/**
 * AbstractIndex的具体实现类
 */
public class Index extends AbstractIndex {
    /**
     * 返回索引的字符串表示
     *
     * @return 索引的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("docIdToDocPathMapping映射为：\n");
        if (docIdToDocPathMapping == null) {
            s.append("空");
        } else {
            for (Map.Entry<Integer, String> entry : docIdToDocPathMapping.entrySet()) {
                s.append(entry.getKey());
                s.append(" ");
                s.append(entry.getValue());
                s.append("\n");
            }
        }
        s.append("termToPostingListMapping倒排索引为：\n");
        if (termToPostingListMapping == null) {
            s.append("空");
        } else {
            for (Map.Entry<AbstractTerm, AbstractPostingList> entry : termToPostingListMapping.entrySet()) {
                s.append(entry.getKey().toString());
                s.append(" ");
                s.append(entry.getValue().toString());
                s.append("\n");
            }
        }
        return s.toString();
    }

    /**
     * 添加文档到索引，更新索引内部的HashMap
     *
     * @param document ：文档的AbstractDocument子类型表示
     */
    @Override
    public void addDocument(AbstractDocument document) {
        if (docIdToDocPathMapping == null) {
            docIdToDocPathMapping = new TreeMap<>();
        }
        docIdToDocPathMapping.put(document.getDocId(), document.getDocPath());
        if (termToPostingListMapping == null) {
            termToPostingListMapping = new TreeMap<AbstractTerm, AbstractPostingList>();
        }
        for (AbstractTermTuple termTuple : document.getTuples()) {
            AbstractPostingList list = termToPostingListMapping.get(termTuple.term);
            if (list == null) {
                list = new PostingList(){};
            }
            boolean flag = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDocId() == document.getDocId()) {
                    flag = true;
                    AbstractPosting now = list.get(i);
                    if (now.getPositions().contains(termTuple.curPos)) {
                        break;
                    } else {
                        now.getPositions().add(termTuple.curPos);
                        now.setFreq(now.getFreq()+1);
                        break;
                    }
                }
            }
            if (!flag) {
                List<Integer> temp = new ArrayList();
                temp.add(termTuple.curPos);
                Posting posting = new Posting(document.getDocId(), termTuple.freq, temp);
                list.add(posting);
            }
        }
    }

    /**
     * <pre>
     * 从索引文件里加载已经构建好的索引.内部调用FileSerializable接口方法readObject即可
     * @param file ：索引文件
     * </pre>
     */
    @Override
    public void load(File file) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        readObject(objectInputStream);
    }

    /**
     * <pre>
     * 将在内存里构建好的索引写入到文件. 内部调用FileSerializable接口方法writeObject即可
     * @param file ：写入的目标索引文件
     * </pre>
     */
    @Override
    public void save(File file) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        writeObject(objectOutputStream);
    }

    /**
     * 返回指定单词的PostingList
     *
     * @param term : 指定的单词
     * @return ：指定单词的PostingList;如果索引字典没有该单词，则返回null
     */
    @Override
    public AbstractPostingList search(AbstractTerm term) {
        return termToPostingListMapping.get(term);
    }

    /**
     * 返回索引的字典.字典为索引里所有单词的并集
     *
     * @return ：索引中Term列表
     */
    @Override
    public Set<AbstractTerm> getDictionary() {
        Set set=new HashSet();
        for (Map.Entry<AbstractTerm, AbstractPostingList> entry : termToPostingListMapping.entrySet()) {
            set.add(entry.getKey());
        }
        return set;
    }

    /**
     * <pre>
     * 对索引进行优化，包括：
     *      对索引里每个单词的PostingList按docId从小到大排序
     *      同时对每个Posting里的positions从小到大排序
     * 在内存中把索引构建完后执行该方法
     * </pre>
     */
    @Override
    public void optimize() {
        for (Map.Entry<AbstractTerm, AbstractPostingList> entry : termToPostingListMapping.entrySet()) {
            AbstractPostingList list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).sort();
            }
            list.sort();
        }
    }

    /**
     * 根据docId获得对应文档的完全路径名
     *
     * @param docId ：文档id
     * @return : 对应文档的完全路径名
     */
    @Override
    public String getDocName(int docId) {
        return docIdToDocPathMapping.get(docId);
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            out.writeObject(this.docIdToDocPathMapping);
            out.writeObject(this.termToPostingListMapping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从二进制文件读
     *
     * @param in ：输入流对象
     */
    @Override
    public void readObject(ObjectInputStream in) {
        try {
            this.docIdToDocPathMapping = (Map<Integer, String>) (in.readObject());
            this.termToPostingListMapping = (Map<AbstractTerm, AbstractPostingList>) (in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
