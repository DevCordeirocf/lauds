package lauds.Model;

import java.util.ArrayList;
import java.util.List;

public class EstadoLaudo {
    public String numeroOS = "";
    public String localizacao = "";
    public String assinatura = "";
    public String problema = "";
    public boolean placaMaeDanificada;
    public String valorPlaca = "";
    public List<EstadoComponente> componentes = new ArrayList<>();
    public List<EstadoDisco> discos = new ArrayList<>();

    public static class EstadoComponente {
        public String nome = "";
        public boolean ok = true;
        public boolean verificado = false;
        public String gravidade = "NECESSARIO";
        public String codigo = "";
    }

    public static class EstadoDisco {
        public String nome = "";
        public boolean ok = true;
        public String saude = "";
        public String gravidade = "RECOMENDADO";
        public boolean comBackup = true;
    }
}
