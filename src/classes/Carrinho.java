package classes;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import models.Produto;

/**
 *
 * @author Luan
 */
public class Carrinho {
    private int cod;
    private double preco;
    private ArrayList<ProdutoQnt> lista;
    private Date date;
    
    public Carrinho(){
        this.cod = this.getLastIdVenda() + 1;
        this.preco = 0.0;
        this.lista = new ArrayList<>();
        this.date = Date.valueOf(LocalDate.now());
    }
    
    public double getPreco(){ return this.preco; }
    
    public String getPrecoFormated(){
        return String.format("%.2f", this.preco).replace(".", ",");
    }
    
    public int getCod(){ return this.cod; }
    
    public Date getDate(){ return this.date; }
    
    private int getLastIdVenda(){
        Connection conexao = Conexao.getConnection();
        String query = "SELECT * FROM VENDA";
        PreparedStatement sttm;
        
        try{
            sttm = conexao.prepareStatement(query);
            ResultSet result = sttm.executeQuery();
            result.last();
            
            return result.getInt("id");
            
        }catch (SQLException ex){
            if(ex.toString().contains("empty result set"))
                return 0;
            System.out.println("Error getLastIdVenda: " + ex);
            return -1;
        }
    }
    
    public void addProduto(Produto produto, int qnt){
        this.addProduto(produto.getCod(), qnt);
    }
    
    public ProdutoQnt addProduto(String cod, int qnt){
        ProdutoQnt produtoNovo = new ProdutoQnt(cod, qnt);
        
        if (produtoNovo.getNome() == null){
            return null;
        }
       
        lista.add(produtoNovo);
        this.preco += produtoNovo.getPrecoDouble() * produtoNovo.getQuantidade();
        
        return produtoNovo;
    }
    
    public void removerProduto(ProdutoQnt produto){
        this.preco = this.preco - produto.getQuantidade() * produto.getPrecoDouble();
        
        for(int i = 0; i < lista.size(); i++){
            if (lista.get(i).igual(produto)){
                lista.remove(i);
                break;
            }
        }
        
        for(int i = 0; i < lista.size(); i++)
            System.out.println(lista.get(i).getNome());
    }
    
    public ProdutoQnt getLastProduto(){
        return this.getProduto(lista.size() - 1);
    }
    
    public ProdutoQnt getProduto(int index){
        return lista.get(index);
    }
    
    public void limpar(){
        lista.clear();
        this.preco = 0.0;
    }
    
    public String finalizarCompra(int vendedor){
        return this.finalizarCompra(0, vendedor);
    }
    
    public String finalizarCompra(int cliente, int vendedor){
        if(!this.inserirVenda(cliente, vendedor))
            return "Error ao inserir a venda!";
        
        if(!this.inserirProduto())
            return "Error ao inserir produto";
       
        return "Compra finalizada com sucesso!";
    }
    
    private boolean inserirProduto(){
        Connection conn = Conexao.getConnection();
        String query = "INSERT INTO item_venda(id_venda, id_produto, preco_produto, qnt_produto) "
                + "values(?, ?, ?, ?)";
        for(ProdutoQnt produto: lista){
            try{
                PreparedStatement sttm = conn.prepareStatement(query);
                
                sttm.setInt(1, this.cod);
                sttm.setString(2, produto.getCod());
                sttm.setString(3, produto.getPreco().replace(',', '.'));
                sttm.setInt(4, produto.getQuantidade());
                
                sttm.executeUpdate();
                
            }catch(SQLException ex){
                System.out.println("Error inserir produto " + ex);
                return false;
            }
        }
        return true;
    }
    
    private boolean inserirVenda(int cliente, int vendedor){
        Connection conn = Conexao.getConnection();
        String query = "INSERT INTO VENDA VALUES(?, ?, ?, ?)";
        PreparedStatement sttm;
        
        try{
            sttm = conn.prepareStatement(query);
            
            sttm.setInt(1, this.cod);
            sttm.setInt(2, cliente);
            sttm.setInt(3, vendedor);
            sttm.setDate(4, this.date);
            
            sttm.executeUpdate();
            return true;
        }catch(SQLException ex){
            throw new RuntimeException("Error InserirVenda: " + ex);
        }
        
    }
}