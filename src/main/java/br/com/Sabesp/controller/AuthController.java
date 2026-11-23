package br.com.Sabesp.controller;

import br.com.Sabesp.dto.LoginRequestDTO;
import br.com.Sabesp.dto.RecuperarSenhaDTO;
import br.com.Sabesp.dto.UsuarioRequestDTO;
import br.com.Sabesp.dto.UsuarioResponseDTO;
import br.com.Sabesp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pela autenticação e gestão de acesso dos usuários
 * da API de chamados de esgoto (Secretaria de Meio Ambiente de Santos).
 *
 * <p>Expõe os endpoints de login, registro de novas contas e recuperação
 * de senha. Após o login, o cliente deve enviar os cabeçalhos
 * {@code X-User-Role} (perfil: COMUM, SABESP ou PREFEITURA) e
 * {@code X-User-Id} nas demais chamadas, pois é por eles que o RBAC
 * (controle de acesso por perfil) é aplicado nos demais controllers.</p>
 *
 * @author Equipe Sabesp
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Realiza o login de um usuário no sistema.
     *
     * <p><b>Verbo HTTP:</b> POST
     * <br><b>Rota:</b> {@code /api/auth/login}
     * <br><b>Acesso:</b> público (qualquer perfil válido: COMUM, SABESP ou PREFEITURA).
     * O perfil retornado é o que deve ser enviado no cabeçalho {@code X-User-Role}
     * nas demais requisições.</p>
     *
     * @param dto credenciais de acesso (e-mail e senha)
     * @return {@code 200 OK} com os dados do usuário autenticado (sem a senha)
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * <p><b>Verbo HTTP:</b> POST
     * <br><b>Rota:</b> {@code /api/auth/registro}
     * <br><b>Acesso:</b> público — exclusivo de cidadãos. O perfil é sempre
     * forçado a {@code COMUM} pelo serviço, mesmo que a requisição envie outro
     * valor. Contas institucionais ({@code SABESP}/{@code PREFEITURA}) só são
     * criadas pela Prefeitura em {@code POST /api/usuarios}; a Sabesp não cria
     * conta de ninguém.</p>
     *
     * @param dto dados do usuário a ser criado
     * @return {@code 201 CREATED} com os dados da conta criada;
     *         {@code 409 CONFLICT} ou {@code 400 BAD REQUEST} se o usuário já
     *         existir ou os dados forem inválidos
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    /**
     * Recupera a senha do usuário pelo e-mail informado.
     * Recebe o e-mail da conta e a nova senha desejada.
     *
     * <p><b>Verbo HTTP:</b> POST
     * <br><b>Rota:</b> {@code /api/auth/recuperar-senha}
     * <br><b>Acesso:</b> público (não exige sessão prévia).</p>
     *
     * @param dto e-mail da conta e nova senha desejada
     * @return {@code 200 OK} com os dados do usuário atualizado;
     *         {@code 404 NOT FOUND} se não existir conta com o e-mail informado
     */
    @PostMapping("/recuperar-senha")
    public ResponseEntity<UsuarioResponseDTO> recuperarSenha(@RequestBody RecuperarSenhaDTO dto) {
        return ResponseEntity.ok(authService.recuperarSenha(dto));
    }
}

