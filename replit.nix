{ pkgs }:

pkgs.mkShell {
  buildInputs = [
    pkgs.openjdk17
    pkgs.maven
    pkgs.mysql80
    pkgs.git
  ];

  shellHook = ''
    echo "🚀 Daily News 项目环境已准备就绪！"
    echo "📦 Java版本: $(java -version 2>&1 | head -n 1)"
    echo "🔧 Maven版本: $(mvn -version | head -n 1)"
    echo "🗄️ MySQL版本: $(mysql --version)"
    echo ""
    echo "💡 使用说明:"
    echo "  - 编译项目: mvn clean package"
    echo "  - 运行项目: bash run.sh"
    echo "  - 查看日志: tail -f logs/news.log"
    echo ""
  '';
}