package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.AbstractPosting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

public class Posting extends AbstractPosting {

    public  Posting(int docId, int frq, List<Integer> pos) {
        this.docId = docId;
        this.freq = frq;
        this.positions = pos;
    }
    /**
     * 判断二个Posting内容是否相同
     * @param obj ：要比较的另外一个Posting
     * @return 如果内容相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof AbstractPosting)) {
            return false;
        }
        return this.docId == ((AbstractPosting) obj).getDocId();
    }
    /**
     * 返回Posting的字符串表示
     * @return 字符串
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Posting内容：");
        s.append("docId:");
        s.append(this.docId);
        s.append("\n");
        s.append("freq:");
        s.append(this.freq);
        s.append("\n");
        s.append("positions为");
        for (int i = 0; i < this.positions.size(); i++) {
            s.append(this.positions.get(i));
            s.append("\n");
        }
        return s.toString();
    }
    /**
     * 返回包含单词的文档id
     * @return ：文档id
     */
    @Override
    public int getDocId() {
        return this.docId;
    }
    /**
     * 设置包含单词的文档id
     * @param docId：包含单词的文档id
     */
    @Override
    public void setDocId(int docId) {
        this.docId = docId;
    }
    /**
     * 返回单词在文档里出现的次数
     * @return ：出现次数
     */
    @Override
    public int getFreq() {
        return this.freq;
    }
    /**
     * 设置单词在文档里出现的次数
     * @param freq:单词在文档里出现的次数
     */
    @Override
    public void setFreq(int freq) {
        this.freq = freq;
    }
    /**
     * 返回单词在文档里出现的位置列表
     * @return ：位置列表
     */
    @Override
    public List<Integer> getPositions() {
        return this.positions;
    }
    /**
     * 设置单词在文档里出现的位置列表
     * @param positions：单词在文档里出现的位置列表
     */
    @Override
    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }
    /**
     * 比较二个Posting对象的大小（根据docId）
     * @param o： 另一个Posting对象
     * @return ：二个Posting对象的docId的差值
     */
    @Override
    public int compareTo(AbstractPosting o) {
        if (this.docId == o.getDocId()) {
            return 0;
        }
        return docId < o.getDocId() ? -1 : 1;
    }
    /**
     * 对内部positions从小到大排序
     */
    @Override
    public void sort() {
        Collections.sort(positions);
    }
    /**
     * 写到二进制文件
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            out.writeObject(this.docId);
            out.writeObject(this.freq);
            out.writeObject(this.positions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 从二进制文件读
     * @param in ：输入流对象
     */
    @Override
    public void readObject(ObjectInputStream in) {
        try {
            this.docId = (int) (in.readObject());
            this.freq = (int) (in.readObject());
            this.positions = (List<Integer>) (in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
